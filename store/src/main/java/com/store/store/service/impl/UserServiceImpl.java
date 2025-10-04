package com.store.store.service.impl;

import com.store.store.component.UserMapper;
import com.store.store.domain.entity.User;
import com.store.store.dto.UserDTO;
import com.store.store.repository.UserRepository;
import com.store.store.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("userServiceImpl")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO.UpdateRequest> getAllUsers() {

        return userRepository.findAll().stream()
                .map(UserMapper::toUpdateRequest)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO.Response signup(UserDTO.Request request) {
        User user = userMapper.toEntity(request, passwordEncoder);
        User saved = userRepository.save(user);

        return userMapper.toResponse(saved);
    }
}
