package com.covoiturage.service.driver;

import com.covoiturage.dto.request.DriverRegistrationRequestDTO;
import com.covoiturage.dto.response.DriverResponseDTO;
import com.covoiturage.entity.Driver;
import com.covoiturage.entity.User;
import com.covoiturage.enums.UserRole;
import com.covoiturage.exception.ResourceAlreadyExistsException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.DriverMapper;
import com.covoiturage.repository.DriverRepository;
import com.covoiturage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final DriverMapper driverMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Driver getDriverByUserId(Long userId) {
        return driverRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found"));
    }

    @Override
    @Transactional
    public DriverResponseDTO registerDriver(DriverRegistrationRequestDTO dto) {
        //check driver
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("Driver already exists");
        }
        //check license number
        if(driverRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new ResourceAlreadyExistsException("Driver already exists");
        }
        //create user
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        // assign driver role
        user.setRole(UserRole.DRIVER);

        User savedUser = userRepository.save(user);

        //  create driver
        Driver driver = new Driver();

        driver.setUser(savedUser);
        driver.setLicenseNumber(dto.getLicenseNumber());

        Driver savedDriver = driverRepository.save(driver);

        // convert entity to response DTO
        return driverMapper.toResponseDTO(savedDriver);
    }

    @Override
    public boolean existsByLicenseNumber(String licenseNumber) {
        return driverRepository.existsByLicenseNumber(licenseNumber);
    }
}