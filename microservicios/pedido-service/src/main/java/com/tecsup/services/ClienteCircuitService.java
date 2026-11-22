package com.tecsup.services;

import com.tecsup.client.ClienteClient;
import com.tecsup.dto.ClienteDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Protege las llamadas de pedido-service hacia cliente-service con Retry + Circuit Breaker.
// Orden de Resilience4j: Retry( CircuitBreaker( llamada ) ) -> el fallback va solo en @Retry
// para que cada intento fallido llegue al Retry y se vuelva a intentar.
@Service
public class ClienteCircuitService {

    @Autowired
    private ClienteClient clienteClient;

    @Retry(name = "clienteRetry", fallbackMethod = "clienteFallback")
    @CircuitBreaker(name = "clienteService")
    public ClienteDTO obtenerCliente(Long id) {
        System.out.println(java.time.LocalTime.now().withNano(0) + " Llamando a cliente-service...");
        return clienteClient.obtenerCliente(id);
    }

    public ClienteDTO clienteFallback(Long id, Exception ex) {
        System.out.println(java.time.LocalTime.now().withNano(0) + " Fallback cliente ejecutado: "
                + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        ClienteDTO dto = new ClienteDTO();
        dto.setId(id);
        dto.setNombre("Cliente temporal no disponible");
        return dto;
    }
}
