package com.c24_39_t_webapp.restaurants.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para TODA la aplicación.
 * <p>
 * ARQUITECTURA:
 * - ApplicationException → Todas las excepciones de negocio (definidas por el usuario)
 * - MethodArgumentNotValidException → Todas las validaciones fallidas (400)
 * - Exception → Cualquier otra excepción (manejadas como 500)
 * <p>
 * Cada excepción de negocio define su propio HttpStatus.
 * Este handler únicamente mapea ApplicationException, delegando
 * la responsabilidad de definir el estado HTTP a cada excepción específica.
 * @see ApplicationException
 * @see MethodArgumentNotValidException
 * FUNDAMENTO: Separación de Responsabilidades (SOLID)
 *
 * ├── ApplicationException.java           ← Clase base
 * │
 * ├── NotFoundException.java              ← PADRE (hereda de ApplicationException)
 * ├── CategoryNotFoundException.java       ├─ hereda de NotFoundException
 * ├── OrderNotFoundException.java          ├─ hereda de NotFoundException
 * ├── ProductNotFoundException.java        ├─ hereda de NotFoundException
 * ├── RestaurantNotFoundException.java     ├─ hereda de NotFoundException
 * └── UserNotFoundException.java           └─ hereda de NotFoundException
 * │
 * ├── UnauthorizedAccessException.java    ← Hereda de ApplicationException
 * ├── ResourceNotFoundException.java       ← Hereda de ApplicationException
 * │
 * └── GlobalExceptionHandler.java         ← MAPEA todas al mapear ApplicationException
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
 * Maneja todas las excepciones de negocio de la aplicación.
 * Cada excepción define su propio HttpStatus en el constructor.
 * - Single Responsibility: Este handler solo mapea ApplicationException (SOLID)
 * - Open/Closed: Abierto a nuevas excepciones (sin modificar este código) (SOLID)
 * @param e la excepción de negocio capturada
 * @return ResponseEntity con status HTTP y cuerpo de error
 * @see ApplicationException#getHttpStatus()
 */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Map<String, Object>> handleApplicationException(ApplicationException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", e.getClass().getSimpleName());
        response.put("message", e.getMessage());
        response.put("timestamp", new Date());
        response.put("status", e.getHttpStatus().value());

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }
    /**
     * Maneja excepciones de validación generadas por Spring
     * lanzadas cuando los datos del cliente fallan validaciones @Valid.
     * Ejemplo: POST /restaurant con email="invalidoSinArroba"
     * Siempre devuelve 400 Bad Request (solicitud es sintácticamente incorrecta)
     * @param e la excepción de validación capturada
     * @return ResponseEntity con 400 y lista de errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validación fallida");
        response.put("message", e.getBindingResult().getAllErrors().stream()
//                .map(error -> error.getDefaultMessage())
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList()));
        response.put("timestamp", new Date());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    /**
     * Maneja cualquier excepción no prevista (catch-all final)
     * como NullPointerException, RuntimeException genérica o cualquiera no manejado explícitamente.
     * Si no hay handler específico, este captura el error y devuelve 500.
     * @param e la excepción no manejada capturada
     * @return ResponseEntity con 500 y mensaje genérico (sin detalles internos)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Error interno del servidor");
        response.put("message", "Se ha producido un error inesperado");
        response.put("timestamp", new Date());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}