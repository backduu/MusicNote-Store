package com.store.store.component;

import com.store.store.domain.entity.User;
import com.store.store.domain.enums.UserStatus;
import com.store.store.dto.UserDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {
    // 엔티티 → DTO 변환
    public static UserDTO.UpdateRequest toUpdateRequest(User user) {
        return UserDTO.UpdateRequest.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLogin(user.getLastLogin())
                .deleted(user.getDeleted())
                .approvedAt(user.getApprovedAt())
                .created(user.getCreated())
                .updated(user.getUpdated())
                .build();
    }

    // dto -> entity
    public User toEntity(UserDTO.Request dto, PasswordEncoder passwordEncoder) {
        return User.builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .role(dto.getRole())
                .status(dto.getStatus())
                .lastLogin(dto.getLastLogin())
                .deleted(dto.getDeleted())
                .approvedAt(dto.getApprovedAt())
                .created(LocalDateTime.now())
                .updated(null)
                .build();
    }

    // entity -> dto
    public UserDTO.Response toResponse(User user) {
        return UserDTO.Response.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .email(user.getEmail())
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
