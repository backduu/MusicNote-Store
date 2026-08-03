package com.store.store.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    INVALID_INPUT("잘못된 입력값"),
    USER_NOT_FOUND("사용자를 찾을 수 없음"),
    INTERNAL_SERVER_ERROR("내부 서버 오류"),
    INVALID_REQUEST("잘못된 요청");

    private final String message;
}
