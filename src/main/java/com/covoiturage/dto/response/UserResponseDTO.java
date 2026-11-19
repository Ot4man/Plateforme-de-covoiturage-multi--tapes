package com.covoiturage.dto.response;

import com.covoiturage.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private LocalDateTime dateCreated;
    private boolean active;
}
