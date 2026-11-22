package com.tecsup.controller;

import com.tecsup.dto.ClienteDTO;
import com.tecsup.dto.PedidosClienteDTO;
import com.tecsup.model.Cliente;
import com.tecsup.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    // Demora simulada para las pruebas de resiliencia de pedido-service (0 = sin demora).
    // Ej.: java -jar cliente-service.jar --simulacion.demora-ms=5000
    @Value("${simulacion.demora-ms:0}")
    private long demoraMs;

    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtener(@PathVariable Long id) throws InterruptedException {
        // Simular que el servicio tarda (5 segundos en la actividad)
        if (demoraMs > 0) {
            System.out.println("Simulando demora de " + demoraMs + " ms para el cliente " + id);
            Thread.sleep(demoraMs);
        }
        return ResponseEntity.ok(service.obtener(id));
    }

    // Pedidos del cliente, consultados en pedido-service
    @GetMapping("/{id}/pedidos")
    public ResponseEntity<PedidosClienteDTO> pedidos(@PathVariable Long id) {
        return ResponseEntity.ok(service.pedidos(id));
    }

    @PostMapping
    public ResponseEntity<Cliente> registrar(@Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Cliente eliminado correctamente"));
    }
}
