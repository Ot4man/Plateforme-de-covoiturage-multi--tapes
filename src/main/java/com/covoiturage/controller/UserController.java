package com.covoiturage.controller;

import com.covoiturage.dto.request.UserRequestDTO;
import com.covoiturage.dto.request.UserUpdateRequestDTO;
import com.covoiturage.dto.response.UserResponseDTO;
import com.covoiturage.entity.User;
import com.covoiturage.service.user.UserServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.covoiturage.mapper.UserMapper;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userServiceImpl;
    private final UserMapper userMapper;
    //create user
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO userResponseDTO = userServiceImpl.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }
    //update user
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO dto) {

        UserResponseDTO response = userServiceImpl.updateUser(id, dto);

        return ResponseEntity.ok(response);
    }
    //delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable Long id) {

        userServiceImpl.deactivateUser(id);

        return ResponseEntity.noContent().build();
    }
    //get all users
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> userResponseDTO = userServiceImpl.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDTO);
    }
    //get use by id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable Long id) {

        User user = userServiceImpl.getUserById(id);

        UserResponseDTO response = userMapper.toResponseDTO(user);

        return ResponseEntity.ok(response);
    }

}
