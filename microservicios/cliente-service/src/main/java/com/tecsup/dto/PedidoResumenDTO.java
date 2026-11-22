package com.tecsup.dto;

import java.time.LocalDate;

// Datos del pedido que llegan desde pedido-service
public class PedidoResumenDTO {

    private Long idPedido;
    private LocalDate fecha;
    private Double total;

    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
