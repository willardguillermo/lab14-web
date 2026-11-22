package com.tecsup.services;

import com.tecsup.client.ClienteClient;
import com.tecsup.client.ProductoClient;
import com.tecsup.dto.DetallePedidoDTO;
import com.tecsup.dto.PedidoDTO;
import com.tecsup.dto.ProductoDTO;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.DetallePedido;
import com.tecsup.model.Pedido;
import com.tecsup.repository.PedidoRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository repo;

    @Autowired
    private ClienteClient clienteClient;

    @Autowired
    private ProductoClient productoClient;

    // Consultas protegidas con Resilience4j (para completar los datos del pedido)
    @Autowired
    private ClienteCircuitService clienteCircuitService;

    @Autowired
    private ProductoCircuitService productoCircuitService;

    public List<Pedido> listar() {
        List<Pedido> pedidos = repo.findAll();
        pedidos.forEach(this::completar);
        return pedidos;
    }

    public Pedido obtener(Long id) {
        return completar(buscar(id));
    }

    @Transactional
    public Pedido registrar(PedidoDTO dto) {
        return completar(repo.save(armar(new Pedido(), dto)));
    }

    @Transactional
    public Pedido actualizar(Long id, PedidoDTO dto) {
        return completar(repo.save(armar(buscar(id), dto)));
    }

    public void eliminar(Long id) {
        repo.delete(buscar(id));
    }

    private Pedido buscar(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
    }

    // Completa los datos del cliente y de cada producto. Si algún servicio falla,
    // Resilience4j devuelve el fallback y el pedido se muestra igual.
    private Pedido completar(Pedido pedido) {
        pedido.setCliente(clienteCircuitService.obtenerCliente(pedido.getIdCliente()));
        pedido.getDetalles().forEach(d ->
                d.setProducto(productoCircuitService.obtenerProducto(d.getIdProducto())));
        return pedido;
    }

    // Valida el cliente y cada producto en sus microservicios y calcula el total
    private Pedido armar(Pedido pedido, PedidoDTO dto) {
        validarCliente(dto.getIdCliente());
        pedido.setFecha(dto.getFecha());
        pedido.setIdCliente(dto.getIdCliente());
        pedido.getDetalles().clear();

        double total = 0;
        for (DetallePedidoDTO d : dto.getDetalles()) {
            ProductoDTO producto = buscarProducto(d.getIdProducto());
            DetallePedido detalle = new DetallePedido();
            detalle.setIdProducto(producto.getId());
            detalle.setCantidad(d.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalle.setPedido(pedido);
            pedido.getDetalles().add(detalle);
            total += producto.getPrecio() * d.getCantidad();
        }
        pedido.setTotal(total);
        return pedido;
    }

    private void validarCliente(Long idCliente) {
        try {
            clienteClient.obtenerCliente(idCliente);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + idCliente);
        }
    }

    private ProductoDTO buscarProducto(Long idProducto) {
        try {
            return productoClient.obtenerProducto(idProducto);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Producto no encontrado con id: " + idProducto);
        }
    }
}
