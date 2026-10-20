package ar.edu.unju.fi.logitrack.shipment.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Traduce las excepciones de la aplicación a respuestas estandarizadas RFC 7807 (application/problem+json).
 * <p>
 * Mapeo de estados HTTP:
 * <ul>
 *    <li>400 – Error de validación en DTOs o datos inválidos</li>
 *    <li>404 – Recurso no encontrado</li>
 *    <li>409 – Conflicto de datos (restricciones de clave única)</li>
 *    <li>422 – Regla de negocio violada</li>
 *    <li>500 – Error interno no esperado</li>
 * </ul>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones cuando no se encuentra un recurso solicitado (HTTP 404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    /**
     * Maneja violaciones a reglas de negocio de la aplicación (HTTP 422).
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio violada", ex.getMessage());
    }

    /**
     * Maneja errores de restricciones de la base de datos como claves duplicadas (HTTP 409).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        return build(HttpStatus.CONFLICT, "Conflicto de datos",
            "La operación viola una restricción de la base de datos (posible dato duplicado).");
    }

    /**
     * Captura y personaliza los errores de validación de campos en DTOs anotados con @Valid (HTTP 400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "La solicitud contiene campos inválidos o incompletos."
        );
        problemDetail.setTitle("Error de Validación");
        problemDetail.setType(URI.create("https://logitrack.unju.edu.ar/errors/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        // Extrae los errores por campo y los añade a las propiedades del ProblemDetail
        Map<String, String> invalidFields = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            invalidFields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("invalidFields", invalidFields);

        return problemDetail;
    }

    /**
     * Captura cualquier otra excepción no controlada en el sistema (HTTP 500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
            "Ocurrió un error inesperado. Intente nuevamente más tarde.");
    }

    /**
     * Método auxiliar para construir objetos ResponseEntity con estructura ProblemDetail.
     */
    private ResponseEntity<ProblemDetail> build(HttpStatus status, String title, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        body.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(body);
    }
}
