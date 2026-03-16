package com.sr.Event_Ingestion_Service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sr.Event_Ingestion_Service.dto.EventResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AssetNotFoundException.class)
    public ResponseEntity<EventResponse> handleAssetNotFoundException(AssetNotFoundException ex) {
        EventResponse response = new EventResponse(null, "REJECTED: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AssetDecommissionedException.class)
    public ResponseEntity<EventResponse> handleAssetDecommissionedException(AssetDecommissionedException ex) {
        EventResponse response = new EventResponse(null, "REJECTED: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EventResponse> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String fieldName = fieldError != null ? fieldError.getField() : "unknown";
        String message = "REJECTED: Invalid input for field: " + fieldName;
        EventResponse response = new EventResponse(null, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<EventResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String fieldName = "unknown";

        if (ex.getCause() instanceof InvalidFormatException invalidFormatException
                && !invalidFormatException.getPath().isEmpty()) {
            fieldName = invalidFormatException.getPath().getFirst().getFieldName();
        }

        String message = "REJECTED: Invalid input for field: " + fieldName;
        EventResponse response = new EventResponse(null, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

