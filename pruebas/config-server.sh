#!/usr/bin/env bash
# Consulta las configuraciones que publica el Config Server y lo que recibió cada microservicio.
C=${CONFIG:-http://localhost:8888}
for s in categoria-service producto-service cliente-service pedido-service; do
  for p in default dev prod; do
    echo "=== $C/$s/$p"
    curl -s "$C/$s/$p" | python3 -m json.tool
  done
done
echo "=== Configuracion recibida por cada microservicio (GET /config)"
for port in 8082 8083 8081 8084; do curl -s "http://localhost:$port/config"; echo; done
