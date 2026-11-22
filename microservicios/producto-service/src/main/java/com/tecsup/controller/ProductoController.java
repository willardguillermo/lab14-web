package com.tecsup.controller;

import com.tecsup.dto.ProductoDTO;
import com.tecsup.model.Producto;
import com.tecsup.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService service;

    // Demora simulada para las pruebas de resiliencia de pedido-service (0 = sin demora).
    // Ej.: java -jar producto-service.jar --simulacion.demora-ms=10000
    @Value("${simulacion.demora-ms:0}")
    private long demoraMs;

    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) throws InterruptedException {
        // Simular que el servicio tarda (10 segundos en la actividad)
        if (demoraMs > 0) {
            System.out.println("Simulando demora de " + demoraMs + " ms para el producto " + id);
            Thread.sleep(demoraMs);
        }
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Producto> registrar(@Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado correctamente"));
    }
}
