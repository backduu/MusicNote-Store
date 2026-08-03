package com.store.store.exception;

import com.store.store.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MusicNoteExceptionHandler {

    @ExceptionHandler(MusicNoteException.class)
    public ResponseEntity<MusicNoteErrorResponse> exceptionHandler(
            MusicNoteException e,
            HttpServletRequest request
    ) {
        log.error("********** ERROR **********");
        log.error("errorCode: {}, url: {}, message: {}", e.getErrorCode(), request.getRequestURL(), e.getMessage());
        log.error(" ");

        MusicNoteErrorResponse response = new MusicNoteErrorResponse(e.getMessage(), e.getErrorCode());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(value = {
            HttpRequestMethodNotSupportedException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<MusicNoteErrorResponse> handleBadRequest(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("********** ERROR **********");
        log.error("url: {}, message: {}", request.getRequestURL(), e.getMessage());
        log.error(" ");

        MusicNoteErrorResponse response = MusicNoteErrorResponse.builder()
                .message(ErrorCode.INVALID_REQUEST.getMessage())
                .errorCode(ErrorCode.INVALID_REQUEST)
                .build();

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MusicNoteErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("********** ERROR **********");
        log.error("url: {}, message: {}", request.getRequestURL(), e.getMessage());
        log.error(" ");

        MusicNoteErrorResponse response = MusicNoteErrorResponse.builder()
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                .build();

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }
}
