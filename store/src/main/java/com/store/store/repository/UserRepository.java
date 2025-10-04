package com.store.store.repository;

import com.store.store.domain.entity.User;
import com.store.store.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 검색
    Optional<User> findByEmail(String email);
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
    // 닉네임 존재 여부 확인
    boolean existsByNickname(String nickname);

    // 상태 별 유저의 이메일이나 이름으로 확인
    List<User> findByStatusAndUsernameOrEmail(UserStatus status, String username, String email);


    // 지정한 시각(dateTime) 이전에 마지막 로그인한 사용자 중, 주어진 상태(status)에 해당하는 사용자 목록 조회
    List<User> findByLastLoginBeforeAndStatus(LocalDateTime dateTime, UserStatus status);
}
