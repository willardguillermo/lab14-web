package com.tecsup.services;

import com.tecsup.client.ClienteClient;
import com.tecsup.dto.ClienteDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Protege las llamadas de pedido-service hacia cliente-service
@Service
public class ClienteCircuitService {

    @Autowired
    private ClienteClient clienteClient;

    @CircuitBreaker(name = "clienteService", fallbackMethod = "clienteFallback")
    public ClienteDTO obtenerCliente(Long id) {
        System.out.println("Llamando a cliente-service...");
        return clienteClient.obtenerCliente(id);
    }

    public ClienteDTO clienteFallback(Long id, Exception ex) {
        System.out.println("Circuit Breaker cliente activado: " + ex.getMessage());
        ClienteDTO dto = new ClienteDTO();
        dto.setId(id);
        dto.setNombre("Cliente temporal no disponible");
        return dto;
    }
}
