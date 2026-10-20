package com.TpGrupalSpring.TpGrupal.DTO;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp, // Cuándo pasó
        int status,              // Código numérico (404, 400, 500)
        String error,            // Nombre del estado ("Not Found", "Bad Request")
        String message,          // El mensaje que vos pusiste en el throw new RuntimeException(...)
        String path              // Qué URL llamó el usuario ("/api/facturas/9999")
) {}
