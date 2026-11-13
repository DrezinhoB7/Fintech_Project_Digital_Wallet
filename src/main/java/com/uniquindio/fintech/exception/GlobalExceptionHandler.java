package com.uniquindio.fintech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler({OperacionInvalidaException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity manejarOperacionInvalida(RuntimeException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> erroresCampos = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            erroresCampos.put(error.getField(), error.getDefaultMessage());
        }
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos.", erroresCampos);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity manejarErrorGeneral(Exception ex) {
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor: " + ex.getMessage(), null);
    }

    private ResponseEntity construirRespuesta(HttpStatus status, String mensaje, Object detalles) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now());
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("mensaje", mensaje);
        if (detalles != null) {
            cuerpo.put("detalles", detalles);
        }
        return ResponseEntity.status(status).body(cuerpo);
    }
}