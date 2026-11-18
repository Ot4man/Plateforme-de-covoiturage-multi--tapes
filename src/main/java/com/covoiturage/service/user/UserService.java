package com.covoiturage.service;

import com.covoiturage.dto.request.UserRequestDTO;
import com.covoiturage.dto.request.UserUpdateRequestDTO;
import com.covoiturage.dto.response.UserResponseDTO;
import com.covoiturage.entity.User;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO dto);

    UserResponseDTO updateUser(Long id, UserUpdateRequestDTO dto);

    void deactivateUser(Long id);

    List<UserResponseDTO> getAllUsers();

    User getUserById(Long id);

    boolean existByEmail(String email);
}