package com.covoiturage.service.driver;

import com.covoiturage.entity.Driver;
import com.covoiturage.dto.request.DriverRegistrationRequestDTO;
import com.covoiturage.dto.response.DriverResponseDTO;

public interface DriverService {

    Driver getDriverByUserId(Long userId);
    DriverResponseDTO registerDriver(DriverRegistrationRequestDTO dto);
    boolean existsByLicenseNumber(String licenseNumber);
}