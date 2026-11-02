package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        // 1. Determinar el código HTTP seguro sin riesgo de NullPointerException
        String rawMsg = ex.getMessage();
        String msg = (rawMsg != null && !rawMsg.isBlank()) ? rawMsg : "Error procesando la solicitud: " + ex.getClass().getSimpleName();
        HttpStatus status = msg.toLowerCase().contains("no encontrad")
                ? HttpStatus.NOT_FOUND      // 404 (No encontrado / no encontrada)
                : HttpStatus.BAD_REQUEST;   // 400 (Cualquier otro error de validación/negocio)


        // 2. Armar el objeto de respuesta con los datos reales
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),             // Ej: 404
                status.getReasonPhrase(),   // Ej: "Not Found"
                msg,                        // Ej: "Factura con ID 9999 no encontrada"
                request.getRequestURI()     // Ej: "/api/facturas/9999"
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrió un error inesperado en el servidor: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }


}
