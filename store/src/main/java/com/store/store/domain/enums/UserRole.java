package com.store.store.domain.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserRole {
    ADMIN("관리자 권한"),
    USER("사용자"),
    SELLER("판매자");

    private final String description;
}
