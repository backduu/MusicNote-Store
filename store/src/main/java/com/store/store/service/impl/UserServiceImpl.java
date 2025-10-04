package com.store.store.service.impl;

import com.store.store.dto.UserDTO;
import com.store.store.repository.UserRepository;
import com.store.store.service.UserService;
import lombok.RequiredArgsConstructor;
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

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO.UpdateRequest> getAllUsers() {

        return userRepository.findAll().stream()
                .map(UserDTO.UpdateRequest::from)
                .toList();
    }
}
