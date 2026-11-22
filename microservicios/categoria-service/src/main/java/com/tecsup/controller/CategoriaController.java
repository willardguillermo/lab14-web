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
    public ResponseEntity<Categoria> obtener(@PathVariable Long id) {
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
