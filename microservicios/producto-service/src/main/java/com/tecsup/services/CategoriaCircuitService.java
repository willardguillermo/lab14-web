package com.tecsup.services;

import com.tecsup.client.CategoriaClient;
import com.tecsup.dto.CategoriaDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Envuelve las llamadas a categoria-service con un Circuit Breaker:
// si categoria-service falla, se responde con una categoría temporal (fallback)
@Service
public class CategoriaCircuitService {

    @Autowired
    private CategoriaClient categoriaClient;

    @CircuitBreaker(name = "categoriaService", fallbackMethod = "categoriaFallback")
    public CategoriaDTO obtenerCategoria(Long id) {
        System.out.println("Llamando a categoria-service...");
        return categoriaClient.obtenerCategoria(id);
    }

    // Mismos parámetros que el método original + la excepción
    public CategoriaDTO categoriaFallback(Long id, Exception ex) {
        System.out.println("Circuit Breaker activado: " + ex.getMessage());
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(id);
        dto.setNombre("Categoría temporal no disponible");
        return dto;
    }
}
