package dev.pedrocosta.sentinel.presentation.api;

import dev.pedrocosta.sentinel.application.exception.IdempotencyConflictException;
import dev.pedrocosta.sentinel.application.exception.RiskAnalysisNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "One or more fields are invalid"
        );
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage());
    }

    @ExceptionHandler({IdempotencyConflictException.class, DataIntegrityViolationException.class})
    ProblemDetail handleConflict(RuntimeException exception) {
        return problem(
                HttpStatus.CONFLICT,
                "Idempotency conflict",
                "This key is already associated with another request or an analysis in progress"
        );
    }

    @ExceptionHandler(RiskAnalysisNotFoundException.class)
    ProblemDetail handleNotFound(RiskAnalysisNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Analysis not found", exception.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://github.com/pedroigor-dev/sentinel-risk-service/problems/"
                + status.value()));
        return problem;
    }
}
