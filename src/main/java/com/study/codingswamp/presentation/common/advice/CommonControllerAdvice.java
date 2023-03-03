package com.study.codingswamp.presentation.common.advice;

import com.study.codingswamp.presentation.common.advice.response.ErrorResponse;
import com.study.codingswamp.exception.CodingSwampException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class CommonControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            BindException.class,
            MethodArgumentNotValidException.class
    })
    public ErrorResponse invalidRequestHandler(BindException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST))
                .message("잘못된 요청입니다.")
                .build();

        for (FieldError fieldError : e.getFieldErrors()) {
            errorResponse.addValidation(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.info("BindException", e);

        return errorResponse;
    }

    @ExceptionHandler(CodingSwampException.class)
    public ResponseEntity<ErrorResponse> codingSwampException(CodingSwampException e) {
        int statusCode = e.getStatusCode();

        ErrorResponse body = ErrorResponse.builder()
                .code(String.valueOf(statusCode))
                .message(e.getMessage())
                .validation(e.getValidation())
                .build();

        log.info("CodingSwampException", e);

        return ResponseEntity
                .status(statusCode)
                .body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError(RuntimeException e) {

        ErrorResponse body = ErrorResponse.builder()
                .code(String.valueOf(INTERNAL_SERVER_ERROR))
                .message(e.getMessage())
                .build();

        log.info("RuntimeException ", e);

        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
