package com.tecsup.services;

import com.tecsup.client.CategoriaClient;
import com.tecsup.dto.CategoriaDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Llamadas a categoria-service protegidas con Retry + Circuit Breaker + Bulkhead.
// Resilience4j aplica los aspectos en este orden: Retry( CircuitBreaker( Bulkhead( llamada ) ) ).
// Por eso el fallback va SOLO en @Retry (el más externo): si los aspectos internos también
// tuvieran fallback, atraparían el error en el primer intento y el Retry nunca reintentaría.
@Service
public class CategoriaCircuitService {

    @Autowired
    private CategoriaClient categoriaClient;

    @Retry(name = "categoriaRetry", fallbackMethod = "categoriaFallback")
    @CircuitBreaker(name = "categoriaService")
    @Bulkhead(name = "categoriaBulkhead")   // máximo 2 llamadas simultáneas a categoria-service
    public CategoriaDTO obtenerCategoria(Long id) {
        System.out.println("Llamando a categoria-service...");
        return categoriaClient.obtenerCategoria(id);
    }

    public CategoriaDTO categoriaFallback(Long id, Exception ex) {
        System.out.println("Fallback ejecutado: " + ex.getClass().getSimpleName());
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(id);
        dto.setNombre("Categoría temporal no disponible");
        return dto;
    }
}
