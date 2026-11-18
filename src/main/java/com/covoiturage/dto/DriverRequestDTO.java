package com.covoiturage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class DriverRequestDTO {
    @NotBlank
    private String licenseNumber;
}

