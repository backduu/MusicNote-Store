package com.store.store.exception;

import com.store.store.domain.enums.ErrorCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MusicNoteErrorResponse {
    private String message;
    private ErrorCode errorCode;
}
