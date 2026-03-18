package com.example.inventory.config;

import com.example.inventory.dto.ApiResponseDTO;
import com.example.inventory.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * MUST override — not @ExceptionHandler — because parent class already maps this exception.
     * Adding a second @ExceptionHandler for the same type is ambiguous in Spring 6.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        logger.error("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            errors.put(fieldName, error.getDefaultMessage());
        });

        ApiResponseDTO<?> response = ApiResponseDTO.builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .statusCode(400)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        logger.error("Resource not found: {}", ex.getMessage());
        return build(ex.getMessage(), 404, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {
        logger.error("Duplicate resource: {}", ex.getMessage());
        return build(ex.getMessage(), 409, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleInsufficientStockException(
            InsufficientStockException ex, HttpServletRequest request) {
        logger.error("Insufficient stock: {}", ex.getMessage());
        return build(ex.getMessage(), 400, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        logger.error("Business error [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return build(ex.getMessage(), 400, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleInvalidCredentialsException(
            InvalidCredentialsException ex, HttpServletRequest request) {
        logger.error("Invalid credentials: {}", ex.getMessage());
        return build(ex.getMessage(), 401, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleInvalidTokenException(
            InvalidTokenException ex, HttpServletRequest request) {
        logger.error("Invalid token: {}", ex.getMessage());
        return build(ex.getMessage(), 401, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleTokenExpiredException(
            TokenExpiredException ex, HttpServletRequest request) {
        logger.error("Token expired: {}", ex.getMessage());
        return build(ex.getMessage(), 401, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request) {
        logger.error("Unauthorized: {}", ex.getMessage());
        return build(ex.getMessage(), 403, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        logger.error("Authentication error: {}", ex.getMessage());
        return build("Authentication failed", 401, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<?>> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error: ", ex);
        return build("An unexpected error occurred", 500, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponseDTO<?>> build(String message, int code, HttpStatus status) {
        return new ResponseEntity<>(
                ApiResponseDTO.builder()
                        .success(false)
                        .message(message)
                        .statusCode(code)
                        .timestamp(System.currentTimeMillis())
                        .build(),
                status);
    }
}