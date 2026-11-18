package com.covoiturage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverResponseDTO {

    private Long id;
    private Long userId;
    private String licenseNumber;
}
