
package com.covoiturage.repository;

import com.covoiturage.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByDriverId(Long driverId);

    Optional<Vehicle> findByImmatriculation(String immatriculation);

    boolean existsByImmatriculation(String immatriculation);
}
