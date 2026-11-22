package com.tecsup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

// @EnableConfigServer convierte la aplicación en un servidor de configuración:
// lee los .yml del repositorio Git y los publica en /{aplicacion}/{perfil}
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
