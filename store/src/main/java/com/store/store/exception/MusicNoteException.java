package com.store.store.exception;

import com.store.store.domain.enums.ErrorCode;
import lombok.Getter;

@Getter
public class MusicNoteException extends RuntimeException {

    // 상세 에러 메시지
    private String message;
    private ErrorCode errorCode;

    public MusicNoteException(String message) {
        super(message);

        this.message = message;
    }

    public MusicNoteException(String message, ErrorCode errorCode) {
        super(errorCode.getMessage());

        this.message = message;
        this.errorCode = errorCode;
    }

    public MusicNoteException(ErrorCode errorCode) {
        super(errorCode.getMessage());

        this.errorCode = errorCode;
    }
}
