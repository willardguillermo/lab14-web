package com.tecsup.services;

import com.tecsup.dto.ClienteDTO;
import com.tecsup.dto.PedidosClienteDTO;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.Cliente;
import com.tecsup.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repo;

    @Autowired
    private PedidoCircuitService pedidoCircuitService;

    public List<Cliente> listar() {
        return repo.findAll();
    }

    public Cliente obtener(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }

    // Primero valida que el cliente exista (404); luego consulta sus pedidos con Circuit Breaker
    public PedidosClienteDTO pedidos(Long id) {
        obtener(id);
        return pedidoCircuitService.obtenerPedidos(id);
    }

    public Cliente registrar(ClienteDTO dto) {
        Cliente c = new Cliente();
        copiar(dto, c);
        return repo.save(c);
    }

    public Cliente actualizar(Long id, ClienteDTO dto) {
        Cliente c = obtener(id);
        copiar(dto, c);
        return repo.save(c);
    }

    public void eliminar(Long id) {
        repo.delete(obtener(id));
    }

    private void copiar(ClienteDTO dto, Cliente c) {
        c.setNombre(dto.getNombre());
        c.setApellidos(dto.getApellidos());
        c.setDireccion(dto.getDireccion());
        c.setTelefono(dto.getTelefono());
        c.setNroDni(dto.getNroDni());
        c.setEstado(dto.getEstado());
    }
}
