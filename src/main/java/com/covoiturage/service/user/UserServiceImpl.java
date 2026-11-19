package com.covoiturage.service.user;

import com.covoiturage.dto.request.UserRequestDTO;
import com.covoiturage.dto.request.UserUpdateRequestDTO;
import com.covoiturage.dto.response.UserResponseDTO;
import com.covoiturage.entity.User;
import com.covoiturage.enums.UserRole;
import com.covoiturage.exception.ResourceAlreadyExistsException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.UserMapper;
import com.covoiturage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(dto);

        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.PASSENGER);

        User savedUser = userRepository.save(user);

        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO dto) {

        User user = getUserById(id);

        if (!user.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());

        User updatedUser = userRepository.save(user);

        return userMapper.toResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {

        User user = getUserById(id);

        user.setActive(false);

        userRepository.save(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Override
    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    @Override
    public boolean existByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}