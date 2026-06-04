package com.abhay.management.site.config;

import com.abhay.management.site.dto.AuthDto;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@Builder
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles @Valid / @Validated bean validation failures.
     * Returns a map of field -> error message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthDto.ApiResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            errors.put(err.getField(), err.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(AuthDto.ApiResponse.builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }

    // Business logic errors (not found, conflict, bad state)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthDto.ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(AuthDto.ApiResponse.error(ex.getMessage()));
    }

    // State errors (user not PENDING, unauthorized action)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<AuthDto.ApiResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(AuthDto.ApiResponse.error(ex.getMessage()));
    }

    // Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthDto.ApiResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthDto.ApiResponse.error("An unexpected error occurred. Please try again."));
    }
}
