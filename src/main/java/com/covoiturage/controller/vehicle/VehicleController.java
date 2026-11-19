package com.covoiturage.controller.vehicle;

import com.covoiturage.dto.request.VehicleRequestDTO;
import com.covoiturage.dto.response.VehicleResponseDTO;
import com.covoiturage.service.vehicule.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponseDTO> createVehicle(
            @Valid @RequestBody VehicleRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehicleService.createVehicle(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(
            @PathVariable Long id) {

        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesByDriverId(
            @PathVariable Long driverId) {

        return ResponseEntity.ok(
                vehicleService.getVehiclesByDriverId(driverId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequestDTO dto) {

        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {

        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
