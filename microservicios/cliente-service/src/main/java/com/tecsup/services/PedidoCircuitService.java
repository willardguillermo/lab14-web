package com.tecsup.services;

import com.tecsup.client.PedidoClient;
import com.tecsup.dto.PedidosClienteDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Protege las llamadas de cliente-service hacia pedido-service
@Service
public class PedidoCircuitService {

    @Autowired
    private PedidoClient pedidoClient;

    @CircuitBreaker(name = "pedidoService", fallbackMethod = "pedidosFallback")
    public PedidosClienteDTO obtenerPedidos(Long idCliente) {
        System.out.println("Llamando a pedido-service...");
        return new PedidosClienteDTO(idCliente, true, "OK", pedidoClient.listarPorCliente(idCliente));
    }

    public PedidosClienteDTO pedidosFallback(Long idCliente, Exception ex) {
        System.out.println("Circuit Breaker pedido activado: " + ex.getMessage());
        return new PedidosClienteDTO(idCliente, false, "Pedidos temporalmente no disponibles", List.of());
    }
}
