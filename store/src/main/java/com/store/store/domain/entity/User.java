package com.store.store.domain.entity;

import com.store.store.domain.enums.UserRole;
import com.store.store.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author  백두현
 * @version 1.0
 * @since   2025-10-02
 * @description UserDetails를 구현한 User 엔티티.
 */

@Table(name = "users")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Getter
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, length = 150)
    private String password;

    @Column(nullable = true, length = 50)
    private String nickname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = true, length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    // TODO 소셜 로그인 제공자에 대한 엔티티 추가 필요?
    // private String socialProvider;

    // TODO BaseTimeEntity로 공통 시간 필드 분리 예정, auditing은 안해주기에 서비스 단에서 업데이트
    private LocalDateTime lastLogin;
    private LocalDateTime deleted;
    private LocalDateTime approvedAt; // status에 따른 승인일자

    @CreatedDate
    private LocalDateTime created;
    @LastModifiedDate
    private LocalDateTime updated;

    // === 비즈니스 로직 === //
    public void approved() {
        this.status = UserStatus.ACTIVE;
        this.approvedAt = LocalDateTime.now();
    }

    public void rejected() {
        this.status = UserStatus.SUSPENDED;
        this.approvedAt = LocalDateTime.now();
    }

    public void deactivated() {
        this.status = UserStatus.INACTIVE;
        this.approvedAt = LocalDateTime.now();
    }

    public void deleted() {
        this.status = UserStatus.DELETED;
        this.approvedAt = LocalDateTime.now();
    }

    public void pending() {
        this.status = UserStatus.PENDING;
        this.approvedAt = LocalDateTime.now();
    }

    public void blocked() {
        this.status = UserStatus.BLOCKED;
        this.approvedAt = LocalDateTime.now();
    }

    // TODO 사용자 마지막 로그인 1년 후 자동 비활성화시키기
    public void updateLastLoginTime() {
        this.lastLogin = LocalDateTime.now();
    }

    public void changeUsername(String newUsername) {
        if (newUsername != null && !newUsername.isBlank()) {
            this.username = newUsername;
        }
    }

    public void initUser(String email,
                         String name,
                         String nickname,
                         String phone,
                         UserRole role,
                         UserStatus status) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.updated = LocalDateTime.now();
        this.created = LocalDateTime.now();
    }

    public void changeNickname(String newNickname) {
        if (newNickname != null && !newNickname.isBlank()) {
            this.nickname = newNickname;
        }
    }

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }

    // === UserDetails 구현 메소드 === //
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isEnabled() { return status == UserStatus.ACTIVE; }
}
