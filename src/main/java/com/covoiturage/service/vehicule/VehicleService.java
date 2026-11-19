package com.covoiturage.service.vehicule;

import com.covoiturage.dto.request.VehicleRequestDTO;
import com.covoiturage.dto.response.VehicleResponseDTO;

import java.util.List;

public interface VehicleService {

    VehicleResponseDTO createVehicle(VehicleRequestDTO dto);

    VehicleResponseDTO getVehicleById(Long id);

    List<VehicleResponseDTO> getVehiclesByDriverId(Long driverId);

    VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO dto);

    void deleteVehicle(Long id);
}
