package com.covoiturage.mapper;

import com.covoiturage.dto.request.VehicleRequestDTO;
import com.covoiturage.dto.response.VehicleResponseDTO;
import com.covoiturage.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleResponseDTO toResponseDTO(Vehicle vehicle) {
        VehicleResponseDTO dto = new VehicleResponseDTO();

        dto.setId(vehicle.getId());
        dto.setMarque(vehicle.getMarque());
        dto.setModele(vehicle.getModele());
        dto.setImmatriculation(vehicle.getImmatriculation());
        dto.setCouleur(vehicle.getCouleur());
        dto.setCapacite(vehicle.getCapacite());
        dto.setDriverId(vehicle.getDriver().getId());

        return dto;
    }

    public Vehicle toEntity(VehicleRequestDTO dto) {
        Vehicle vehicle = new Vehicle();

        vehicle.setMarque(dto.getMarque());
        vehicle.setModele(dto.getModele());
        vehicle.setImmatriculation(dto.getImmatriculation());
        vehicle.setCouleur(dto.getCouleur());
        vehicle.setCapacite(dto.getCapacite());

        return vehicle;
    }
}
