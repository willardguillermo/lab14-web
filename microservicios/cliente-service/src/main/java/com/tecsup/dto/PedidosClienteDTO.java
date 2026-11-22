package com.tecsup.dto;

import java.util.List;

// "disponible" distingue un cliente sin pedidos de un pedido-service que no responde
public record PedidosClienteDTO(Long idCliente, boolean disponible, String mensaje,
                                List<PedidoResumenDTO> pedidos) {
}
