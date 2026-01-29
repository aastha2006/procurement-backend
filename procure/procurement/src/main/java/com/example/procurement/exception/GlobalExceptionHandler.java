package com.example.procurement.exception;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        ex.printStackTrace(); // Keep validation logs
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Conflict");
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException e) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Conflict");

        String fieldName = extractFieldNameFromException(e.getMessage());
        if (fieldName != null) {
            errorResponse.put("message", "Duplicate value in field: " + fieldName);
        } else {
            errorResponse.put("message", "Duplicate key violation: " + e.getMostSpecificCause().getMessage());
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(error -> {
            String fieldName = error.getPropertyPath().toString();
            String message = error.getMessage();
            errors.put(fieldName, message);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        Map<String, Object> errorResponse = new LinkedHashMap<>();

        errorResponse.put("status", "error");

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        errorResponse.put("message", "Validation failed");
        errorResponse.put("errors", fieldErrors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private String extractFieldNameFromException(String exceptionMessage) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Key \\((.*?)\\)=\\((.*?)\\) already exists");
        java.util.regex.Matcher matcher = pattern.matcher(exceptionMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        ex.printStackTrace();
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
