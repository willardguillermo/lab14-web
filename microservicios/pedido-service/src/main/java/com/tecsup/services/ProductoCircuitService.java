package com.tecsup.services;

import com.tecsup.client.ProductoClient;
import com.tecsup.dto.ProductoDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Protege las llamadas de pedido-service hacia producto-service con Circuit Breaker + Bulkhead.
// El Bulkhead limita a 2 las consultas simultáneas a producto-service: si producto-service
// se pone lento, no se llevan todos los hilos de pedido-service.
@Service
public class ProductoCircuitService {

    @Autowired
    private ProductoClient productoClient;

    @CircuitBreaker(name = "productoService", fallbackMethod = "productoFallback")
    @Bulkhead(name = "productoBulkhead")
    public ProductoDTO obtenerProducto(Long id) {
        System.out.println(java.time.LocalTime.now().withNano(0) + " [" + Thread.currentThread().getName()
                + "] Llamando a producto-service...");
        return productoClient.obtenerProducto(id);
    }

    public ProductoDTO productoFallback(Long id, Exception ex) {
        System.out.println(java.time.LocalTime.now().withNano(0) + " [" + Thread.currentThread().getName()
                + "] Fallback producto ejecutado: " + ex.getClass().getSimpleName());
        ProductoDTO dto = new ProductoDTO();
        dto.setId(id);
        dto.setNombre("Producto temporal no disponible");
        return dto;
    }
}
