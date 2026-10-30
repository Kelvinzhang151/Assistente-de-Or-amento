package br.com.orcamento.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Captura as NegocioException de qualquer controller e responde HTTP 400
 * com a mensagem de erro.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, String>> tratar(NegocioException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }
}
