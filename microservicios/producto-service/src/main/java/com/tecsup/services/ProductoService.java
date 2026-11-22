package com.tecsup.services;

import com.tecsup.client.CategoriaClient;
import com.tecsup.dto.ProductoDTO;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.Producto;
import com.tecsup.repository.ProductoRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repo;

    @Autowired
    private CategoriaClient categoriaClient;

    public List<Producto> listar() {
        List<Producto> productos = repo.findAll();
        productos.forEach(this::cargarCategoria);
        return productos;
    }

    public Producto obtener(Long id) {
        Producto producto = repo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        cargarCategoria(producto);
        return producto;
    }

    public Producto registrar(ProductoDTO dto) {
        validarCategoria(dto.getCategoriaId());
        Producto p = new Producto();
        copiar(dto, p);
        return conCategoria(repo.save(p));
    }

    public Producto actualizar(Long id, ProductoDTO dto) {
        Producto p = obtener(id);
        validarCategoria(dto.getCategoriaId());
        copiar(dto, p);
        return conCategoria(repo.save(p));
    }

    public void eliminar(Long id) {
        repo.delete(obtener(id));
    }

    private void copiar(ProductoDTO dto, Producto p) {
        p.setNombre(dto.getNombre());
        p.setPrecio(dto.getPrecio());
        p.setStock(dto.getStock());
        p.setCategoriaId(dto.getCategoriaId());
    }

    // Antes de guardar, la categoría debe existir en categoria-service
    private void validarCategoria(Long categoriaId) {
        try {
            categoriaClient.obtenerCategoria(categoriaId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Categoria no encontrada con id: " + categoriaId);
        }
    }

    private Producto conCategoria(Producto p) {
        cargarCategoria(p);
        return p;
    }

    // Si categoria-service no responde, el producto se devuelve igual (categoria = null)
    private void cargarCategoria(Producto p) {
        try {
            p.setCategoria(categoriaClient.obtenerCategoria(p.getCategoriaId()));
        } catch (FeignException e) {
            p.setCategoria(null);
        }
    }
}
