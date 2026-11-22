package com.tecsup.services;

import com.tecsup.client.ProductoClient;
import com.tecsup.dto.ProductoDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Protege las llamadas de pedido-service hacia producto-service
@Service
public class ProductoCircuitService {

    @Autowired
    private ProductoClient productoClient;

    @CircuitBreaker(name = "productoService", fallbackMethod = "productoFallback")
    public ProductoDTO obtenerProducto(Long id) {
        System.out.println("Llamando a producto-service...");
        return productoClient.obtenerProducto(id);
    }

    public ProductoDTO productoFallback(Long id, Exception ex) {
        System.out.println("Circuit Breaker producto activado: " + ex.getMessage());
        ProductoDTO dto = new ProductoDTO();
        dto.setId(id);
        dto.setNombre("Producto temporal no disponible");
        return dto;
    }
}
