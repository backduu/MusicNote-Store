package com.store.store.domain.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("정상 활동 중인 회원"),
    INACTIVE("비활성화 (로그인 불가, 휴면)"),
    SUSPENDED("규칙 위반으로 인한 일시 정지된 회원"),
    DELETED("탈퇴 처리된 회원"),
    PENDING("이메일 인증 완료 전"),
    BLOCKED("관리자에 의해 차단된 회원");

    private final String description;
}
