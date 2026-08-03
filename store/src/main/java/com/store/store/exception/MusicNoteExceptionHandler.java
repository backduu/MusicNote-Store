package com.store.store.exception;

import com.store.store.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MusicNoteExceptionHandler {

    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler(MusicNoteException.class)
    public MusicNoteErrorResponse exceptionHandler(
            MusicNoteException e,
            HttpServletRequest request
    ) {
        log.error("********** ERROR **********");
        log.error("errorCode: {}, url: {}, message: {}", e.getErrorCode(), request.getRequestURL(), e.getMessage());
        log.error(" ");

        return new MusicNoteErrorResponse(e.getMessage(), e.getErrorCode());
    }

    @ExceptionHandler(value = {
            HttpRequestMethodNotSupportedException.class,
            MethodArgumentNotValidException.class
    })
    public MusicNoteErrorResponse handleBadRequest(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("********** ERROR **********");
        log.error("url: {}, message: {}", request.getRequestURL(), e.getMessage());
        log.error(" ");

        return MusicNoteErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_REQUEST)
                .message(ErrorCode.INVALID_REQUEST.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    public MusicNoteErrorResponse handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("********** ERROR **********");
        log.error("url: {}, message: {}", request.getRequestURL(), e.getMessage());
        log.error(" ");

        return new MusicNoteErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                ErrorCode.INTERNAL_SERVER_ERROR
        );
    }
}
