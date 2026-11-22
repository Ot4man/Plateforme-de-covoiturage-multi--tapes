package com.covoiturage.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverRegistrationRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank
    private String lastName;


    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    private String phone;

    @NotBlank
    private String licenseNumber;
}