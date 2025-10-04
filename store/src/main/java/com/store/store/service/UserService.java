package com.store.store.service;

import com.store.store.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO.UpdateRequest> getAllUsers();
}
