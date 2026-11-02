package com.TpGrupalSpring.TpGrupal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

@Configuration
public class ViteAutoStartConfig {

    private static final Logger log = LoggerFactory.getLogger(ViteAutoStartConfig.class);
    private static final int VITE_PORT = 5173;

    @EventListener(ApplicationReadyEvent.class)
    public void iniciarViteAlArrancar() {
        if (isPortInUse(VITE_PORT)) {
            log.info("Vite ya se encuentra activo en el puerto {}. No es necesario iniciarlo nuevamente.", VITE_PORT);
            return;
        }

        File frontendDir = new File("frontend");
        if (!frontendDir.exists()) {
            File altDir = new File("Practica/Grupal/frontend");
            if (altDir.exists()) {
                frontendDir = altDir;
            } else {
                log.warn("No se encontro la carpeta 'frontend' para iniciar Vite automaticamente.");
                return;
            }
        }

        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(frontendDir);

        if (os.contains("win")) {
            processBuilder.command("cmd.exe", "/c", "start", "Frontend Vite", "cmd.exe", "/k", "pnpm dev");
        } else {
            processBuilder.command("bash", "-c", "pnpm dev");
        }

        try {
            processBuilder.start();
            log.info("Vite se ha iniciado automaticamente en segundo plano.");
        } catch (IOException e) {
            log.error("Error al iniciar Vite automaticamente: {}", e.getMessage());
        }
    }

    private boolean isPortInUse(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
