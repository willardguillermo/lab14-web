package com.tecsup.client;

import com.tecsup.dto.CategoriaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "categoria-service" es el nombre registrado en Eureka: no hay URL ni puerto fijo
@FeignClient(name = "categoria-service")
public interface CategoriaClient {

    @GetMapping("/api/categorias/{id}")
    CategoriaDTO obtenerCategoria(@PathVariable("id") Long id);
}
