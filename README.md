# Lab 14 - Config Server y Resiliencia

Laboratorio 14 de **Desarrollo de Aplicaciones Web** (Tecsup): *Patrones de Resiliencia y
Configuración Centralizada en Arquitecturas de Microservicios*.

Sobre los microservicios del Lab 13 se agregan:

- **Config Server**: la configuración (puerto, base de datos, JPA, mensajes) de todos los
  microservicios vive en un solo lugar, la carpeta [`config-repo/`](config-repo), con perfiles `dev` y `prod`.
- **Circuit Breaker**: si un servicio consumido falla, se deja de llamarlo y se responde con un *fallback*.
- **Retry**: las llamadas que fallan por causas temporales se reintentan automáticamente.
- **Bulkhead**: se limita cuántas llamadas simultáneas se hacen a un servicio lento, para que no
  se lleve todos los hilos de la aplicación.

```
                         config-server :8888  ◄── lee ── config-repo/ (GitHub)
                               ▲  (spring.config.import)
         ┌─────────────────────┼──────────────────────┬────────────────────┐
 categoria-service :8082  producto-service :8083  cliente-service :8081  pedido-service :8084
                          │ CircuitBreaker            ▲                   │ Retry + CB ──► cliente
                          │ + Retry + Bulkhead        │                   │ Bulkhead + CB ──► producto
                          └──────► categoria          └───────────────────┘
                     todos se registran en eureka-server :8761
```

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.15 |
| Spring Cloud | 2025.0.3 (Config Server, Eureka, OpenFeign) |
| Resilience4j | `resilience4j-spring-boot3` (gestionado por Spring Cloud) |
| MySQL | 8+ |

## Estructura

```
config-repo/                 configuraciones que publica el config-server
├── categoria-service.yml    comun (nombre, JPA, mensaje)
├── categoria-service-dev.yml   puerto 8082 + BD local
├── categoria-service-prod.yml  puerto 9092 + BD de produccion
└── ... (igual para producto, cliente y pedido)
microservicios/
├── config-server      (8888)  @EnableConfigServer
├── eureka-server      (8761)
├── gateway-service    (8080)  (del Lab 13, sin cambios)
├── categoria-service  (8082)
├── producto-service   (8083)  CategoriaCircuitService: CircuitBreaker + Retry + Bulkhead
├── cliente-service    (8081)  PedidoCircuitService: CircuitBreaker hacia pedido-service
└── pedido-service     (8084)  ClienteCircuitService (Retry + CB) y ProductoCircuitService (Bulkhead + CB)
pruebas/                scripts de prueba
```

## Config Server

[`config-server/src/main/resources/application.yaml`](microservicios/config-server/src/main/resources/application.yaml)

```yaml
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/willardguillermo/lab14-web
          search-paths: config-repo
          default-label: main
```

> La guía pide un repositorio aparte llamado `config-repo`. Aquí los archivos están en la carpeta
> `config-repo/` de este mismo repositorio y el Config Server la encuentra con `search-paths`.
> Para probar sin subir cambios se puede apuntar a la copia local:
> `java -jar config-server.jar --spring.cloud.config.server.git.uri=file:///ruta/a/lab14-web`

Cada microservicio solo conserva en su `application.yaml` el nombre, el perfil activo, Eureka y
Resilience4j. Todo lo demás llega del Config Server:

```yaml
spring:
  application:
    name: producto-service
  profiles:
    active: dev
  config:
    import: optional:configserver:http://localhost:8888
```

Los `application-dev.yaml` locales se eliminaron. Cada servicio tiene `GET /config`, que muestra
el mensaje, el puerto, la BD y el perfil que recibió.

## Resiliencia

### producto-service → categoria-service

[`CategoriaCircuitService`](microservicios/producto-service/src/main/java/com/tecsup/services/CategoriaCircuitService.java)

```java
@Retry(name = "categoriaRetry", fallbackMethod = "categoriaFallback")
@CircuitBreaker(name = "categoriaService")
@Bulkhead(name = "categoriaBulkhead")
public CategoriaDTO obtenerCategoria(Long id) { ... }
```

### cliente-service → pedido-service (actividad)

[`PedidoCircuitService`](microservicios/cliente-service/src/main/java/com/tecsup/services/PedidoCircuitService.java):
`GET /api/clientes/{id}/pedidos` devuelve los pedidos del cliente. Si `pedido-service` no responde,
el fallback devuelve `disponible: false` y la lista vacía.

### pedido-service → cliente-service (Retry) y producto-service (Bulkhead)

- [`ClienteCircuitService`](microservicios/pedido-service/src/main/java/com/tecsup/services/ClienteCircuitService.java):
  `@Retry(clienteRetry)` + `@CircuitBreaker(clienteService)`. Feign corta la llamada a cliente-service a los 3 s.
- [`ProductoCircuitService`](microservicios/pedido-service/src/main/java/com/tecsup/services/ProductoCircuitService.java):
  `@CircuitBreaker(productoService)` + `@Bulkhead(productoBulkhead)`.
- `GET /api/pedidos` devuelve cada pedido con su `cliente` y el `producto` de cada detalle.

| Instancia | Configuración |
|---|---|
| Circuit Breaker | ventana de 5 llamadas, mínimo 3, se abre con 50 % de fallos, 10 s abierto |
| Retry | 3 intentos, 2 s entre intentos; no reintenta si el circuito está abierto ni ante un 404 |
| Bulkhead | 2 llamadas simultáneas, 1 s de espera máxima |

### Orden de los aspectos (lo que se encontró en la prueba)

Resilience4j aplica las anotaciones como `Retry( CircuitBreaker( Bulkhead( llamada ) ) )`. Con el
código tal cual la guía (`fallbackMethod` también en `@CircuitBreaker`), el fallback del Circuit
Breaker atrapa el error en el **primer** intento y el Retry nunca reintenta: en la consola sale un
solo `Llamando a categoria-service...` seguido de `Fallback ejecutado`. Por eso el `fallbackMethod`
se dejó solo en la anotación más externa (`@Retry`). Así se ven los 3 intentos y después el fallback.

### Demoras simuladas

Los `GET /{id}` de categoria, cliente y producto aceptan `--simulacion.demora-ms`:

```bash
java -jar categoria-service.jar --simulacion.demora-ms=10000   # Bulkhead de producto-service
java -jar cliente-service.jar   --simulacion.demora-ms=5000    # Retry de pedido-service
java -jar producto-service.jar  --simulacion.demora-ms=10000   # Bulkhead de pedido-service
```

## Cómo ejecutar

1. Crear las bases de datos: `mysql -uroot < crear-bases-datos.sql`
2. Levantar en orden: `config-server`, `eureka-server`, `categoria-service`, `producto-service`,
   `cliente-service`, `pedido-service` (con `./mvnw spring-boot:run` en cada carpeta).
3. Probar:
   ```bash
   ./pruebas/config-server.sh
   ./pruebas/concurrentes.sh http://localhost:8083/api/productos 5
   ./pruebas/concurrentes.sh http://localhost:8084/api/pedidos 5
   ```

## Resultados

El detalle, las capturas y las conclusiones están en el informe:
[`docs/INFORME-LAB14.md`](docs/INFORME-LAB14.md) ([.docx](docs/INFORME-LAB14.docx)).
