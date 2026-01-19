package com.example.forum.core.exception;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // validation error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException (
            MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(
                        false,
                        "validation failed",
                        errors
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, ex.getMessage(), null)
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatusException(ResponseStatusException ex){
        return ResponseEntity.status(ex.getStatusCode()).body(
                new ApiResponse<>(false, ex.getReason(), null)
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(false, ex.getMessage(), null)
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleEmailAlreadyUsed(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiResponse<>(
                        false,
                        "Email was used!",
                        ex.getMessage()
                )
        );
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiResponse<>(false, "Access denied", null)
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex){
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body( new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null

                ));
    }

    @ExceptionHandler(NotLoggedInException.class)
    public ResponseEntity<?> handleNotLoggedInException(NotLoggedInException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        false,
                        e.getMessage(),
                        null
                ));

    }

    @ExceptionHandler(OtpVerificationException.class)
    public ResponseEntity<?> handleOtpVerification(OtpVerificationException e){
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body( new ApiResponse<>(
                        false,
                        e.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeException(MaxUploadSizeExceededException maxUploadSizeExceededException){
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiResponse<>(
                        false,
                        MessageConstants.UPLOAD_LIMIT_EXCEEDED,
                        null
                ));
    }

}
