package com.covoiturage.mapper;

import com.covoiturage.dto.DriverResponseDTO;
import com.covoiturage.entity.Driver;
import org.springframework.stereotype.Component;

@Component
public class DriverMapper {

    public DriverResponseDTO toResponseDTO(Driver driver) {

        DriverResponseDTO dto = new DriverResponseDTO();

        dto.setId(driver.getId());
        dto.setUserId(driver.getUser().getId());
        dto.setLicenseNumber(driver.getLicenseNumber());

        return dto;
    }
}
