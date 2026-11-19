package com.covoiturage.controller.driver;

import com.covoiturage.dto.request.DriverRegistrationRequestDTO;
import com.covoiturage.dto.response.DriverResponseDTO;
import com.covoiturage.service.driver.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverResponseDTO> registerDriver(
            @Valid @RequestBody DriverRegistrationRequestDTO dto) {

        DriverResponseDTO response = driverService.registerDriver(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}