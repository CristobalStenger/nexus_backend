package cl.nexustech.pedido.config;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Todos los errores salen como JSON (RFC 7807): { status, detail, ... }.
// Los de validación agregan "errores": { campo: mensaje } para mostrarlos en el formulario.
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        e -> String.valueOf(e.getDefaultMessage()), (primero, otro) -> primero));
        ProblemDetail detalle = ex.getBody();
        detalle.setDetail("Revisa los datos enviados.");
        detalle.setProperty("errores", errores);
        return handleExceptionInternal(ex, detalle, headers, status, request);
    }
}
