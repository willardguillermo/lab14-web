package com.tecsup.controller;

import com.tecsup.dto.CategoriaDTO;
import com.tecsup.model.Categoria;
import com.tecsup.services.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService service;

    @Value("${empresa.ambiente}")
    private String ambiente;

    // Demora simulada para probar el Bulkhead (0 = sin demora).
    // Ej.: java -jar categoria-service.jar --simulacion.demora-ms=10000
    @Value("${simulacion.demora-ms:0}")
    private long demoraMs;

    // Muestra qué perfil (dev / prod) está activo
    @GetMapping("/ambiente")
    public ResponseEntity<Map<String, String>> ambiente() {
        return ResponseEntity.ok(Map.of("ambiente", ambiente));
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PostMapping
    public ResponseEntity<Categoria> guardar(@Valid @RequestBody CategoriaDTO dto) {
        Categoria c = new Categoria();
        c.setNombre(dto.getNombre());
        c.setEstado(dto.getEstado());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(c));
    }

    // Si no existe, el service lanza ResourceNotFoundException -> 404
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtener(@PathVariable Long id) throws InterruptedException {
        // Simular que el servicio tarda (10 segundos en la prueba del Bulkhead)
        if (demoraMs > 0) {
            System.out.println("Simulando demora de " + demoraMs + " ms para la categoria " + id);
            Thread.sleep(demoraMs);
        }
        return ResponseEntity.ok(service.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(@PathVariable Long id,
                                                @Valid @RequestBody CategoriaDTO dto) {
        Categoria existente = service.obtener(id);
        existente.setNombre(dto.getNombre());
        existente.setEstado(dto.getEstado());
        return ResponseEntity.ok(service.guardar(existente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Eliminado correctamente"));
    }
}
