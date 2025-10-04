package com.store.store.dto;

import com.store.store.domain.entity.User;
import com.store.store.domain.enums.UserRole;
import com.store.store.domain.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class UserDTO {
    // === 회원가입, 회원 정보 수정 === //
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank
        @Size(max = 50)
        private String username;

        @NotBlank
        @Size(min = 8, max = 60, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        private String password;

        @NotBlank
        @Size(max = 50)
        private String nickname;

        @NotBlank
        @Email
        @Size(max = 100)
        private String email;

        @NotNull
        private UserRole role;

    }

    // === 모든 사용자 찾기 === //
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @NotNull
        private Long id;

        @NotBlank
        @Size(max = 50)
        private String username;

        @NotBlank
        @Size(max = 50)
        private String name;

        @NotBlank
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
        private String phone;

        @NotBlank
        @Size(max = 50)
        private String nickname;

        @NotBlank
        @Email
        @Size(max = 100)
        private String email;

        @NotNull
        private UserRole role;

        @NotNull
        private UserStatus status;

        private LocalDateTime lastLogin;
        private LocalDateTime deleted;
        private LocalDateTime approvedAt;
        private LocalDateTime created;
        private LocalDateTime updated;

        public static UpdateRequest from(User user) {
            return UpdateRequest.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLogin(user.getLastLogin())
                .deleted(user.getDeleted())
                .approvedAt(user.getApprovedAt())
                .created(user.getCreated())
                .updated(user.getUpdated())
                .build();
        }
    }

    // === 사용자 응답 DTO === //
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String username;
        private String name;
        private String phone;
        private String nickname;
        private String email;
        private UserRole role;
        private UserStatus status;
        private LocalDateTime lastLogin;
        private LocalDateTime deleted;
        private LocalDateTime approvedAt;
        private LocalDateTime created;
        private LocalDateTime updated;
    }
}
