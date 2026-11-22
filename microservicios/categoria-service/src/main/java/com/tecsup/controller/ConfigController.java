package com.tecsup.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

// Muestra los valores que el microservicio recibió desde el Config Server
@RestController
public class ConfigController {

    @Value("${mensaje.texto:Sin configuración remota}")
    private String mensaje;

    @Value("${server.port}")
    private String puerto;

    @Value("${spring.datasource.url:}")
    private String datasource;

    private final Environment env;

    public ConfigController(Environment env) {
        this.env = env;
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        return ResponseEntity.ok(Map.of(
                "mensaje", mensaje,
                "puerto", puerto,
                "datasource", datasource,
                "perfiles", Arrays.asList(env.getActiveProfiles())));
    }
}
