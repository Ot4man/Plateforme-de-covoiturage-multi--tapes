package com.covoiturage.service.vehicule;

import com.covoiturage.dto.request.VehicleRequestDTO;
import com.covoiturage.dto.response.VehicleResponseDTO;
import com.covoiturage.entity.Driver;
import com.covoiturage.entity.Vehicle;
import com.covoiturage.exception.ResourceAlreadyExistsException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.VehicleMapper;
import com.covoiturage.repository.DriverRepository;
import com.covoiturage.repository.VehicleRepository;

import com.covoiturage.service.vehicule.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    public VehicleResponseDTO createVehicle(VehicleRequestDTO dto) {

        if (vehicleRepository.existsByImmatriculation(dto.getImmatriculation())) {
            throw new ResourceAlreadyExistsException(
                    "Vehicle registration number already exists"
            );
        }

        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Driver not found"
                ));

        Vehicle vehicle = vehicleMapper.toEntity(dto);
        vehicle.setDriver(driver);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return vehicleMapper.toResponseDTO(savedVehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicleById(Long id) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found"
                ));

        return vehicleMapper.toResponseDTO(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getVehiclesByDriverId(Long driverId) {

        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException("Driver not found");
        }

        return vehicleRepository.findByDriverId(driverId)
                .stream()
                .map(vehicleMapper::toResponseDTO)
                .toList();
    }

    @Override
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO dto) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found"
                ));

        boolean registrationChanged =
                !vehicle.getImmatriculation().equals(dto.getImmatriculation());

        if (registrationChanged &&
                vehicleRepository.existsByImmatriculation(dto.getImmatriculation())) {
            throw new ResourceAlreadyExistsException(
                    "Vehicle registration number already exists"
            );
        }

        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Driver not found"
                ));

        vehicle.setMarque(dto.getMarque());
        vehicle.setModele(dto.getModele());
        vehicle.setImmatriculation(dto.getImmatriculation());
        vehicle.setCouleur(dto.getCouleur());
        vehicle.setCapacite(dto.getCapacite());
        vehicle.setDriver(driver);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        return vehicleMapper.toResponseDTO(updatedVehicle);
    }

    @Override
    public void deleteVehicle(Long id) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found"
                ));

        vehicleRepository.delete(vehicle);
    }
}
