package com.covoiturage.service.trip;

import com.covoiturage.dto.request.PointArretRequestDTO;
import com.covoiturage.dto.request.TripRequestDTO;
import com.covoiturage.dto.response.TripResponseDTO;
import com.covoiturage.entity.*;
import com.covoiturage.enums.StatutReservation;
import com.covoiturage.enums.StatutTrajet;
import com.covoiturage.enums.UserRole;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.TripMapper;
import com.covoiturage.repository.DriverRepository;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.repository.TripRepository;
import com.covoiturage.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.covoiturage.entity.Reservation;
import com.covoiturage.enums.StatutReservation;
import com.covoiturage.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final TripMapper tripMapper;
    private final ReservationRepository reservationRepository;

    // Create a trip
    @Override
    public TripResponseDTO createTrip(TripRequestDTO dto) {

        // Retrieve the driver and vehicle
        Driver driver = getDriverById(dto.driverId());

        if (driver.getUser().getRole() != UserRole.DRIVER) {
            throw new IllegalArgumentException(
                    "Only users with DRIVER role can create trips"
            );
        }

        Vehicle vehicle = getVehicleById(dto.vehicleId());
        // Validate stopping points
        validateStoppingPoints(dto.pointsArret());

        // Ensure the vehicle belongs to the driver
        validateVehicleOwnership(vehicle, driver);

        // Validate proposed seats against vehicle capacity
        validateVehicleCapacity(dto.placesProposees(), vehicle);

        // Create the trip entity
        Trip trip = new Trip();
        trip.setDriver(driver);
        trip.setVehicle(vehicle);
        trip.setPlacesProposees(dto.placesProposees());
        trip.setDateCreation(LocalDateTime.now());
        trip.setStatut(StatutTrajet.PUBLIE);

        // Convert and associate stopping points
        List<PointArret> points = dto.pointsArret().stream()
                .map(tripMapper::toEntity)
                .peek(point -> point.setTrajet(trip))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        trip.setPointsArret(points);

        // Save the trip
        Trip savedTrip = tripRepository.save(trip);

        return tripMapper.toResponseDTO(savedTrip);
    }

    // Validate stopping points
    private void validateStoppingPoints(List<PointArretRequestDTO> pointsArret) {

        // Check that the trip contains at least two stopping points
        if (pointsArret == null || pointsArret.size() < 2) {
            throw new IllegalArgumentException(
                    "A trip must contain at least two stopping points");
        }

        Set<String> cities = new HashSet<>();

        LocalDateTime previousTime = null;
        BigDecimal previousPrice = null;

        for (int i = 0; i < pointsArret.size(); i++) {

            PointArretRequestDTO point = pointsArret.get(i);

            // Check required values
            if (point == null || point.ville() == null
                    || point.heurePassage() == null
                    || point.prixDepuisDepart() == null
                    || point.ordre() == null) {
                throw new IllegalArgumentException(
                        "Stopping point fields cannot be null");
            }

            // Check that the city is not blank
            String city = point.ville().trim().toLowerCase();

            if (city.isEmpty()) {
                throw new IllegalArgumentException(
                        "City name cannot be empty");
            }

            // Check stopping point order
            if (point.ordre() != i) {
                throw new IllegalArgumentException(
                        "Stopping point order must start at 0 and be consecutive");
            }

            // Check that cities are unique
            if (!cities.add(city)) {
                throw new IllegalArgumentException(
                        "A city cannot appear more than once in a trip");
            }

            // Check departure time is in the future
            if (i == 0 && !point.heurePassage().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException(
                        "Departure time must be in the future");
            }

            // Check that stopping times are strictly increasing
            if (previousTime != null
                    && !point.heurePassage().isAfter(previousTime)) {
                throw new IllegalArgumentException(
                        "Stopping times must be strictly increasing");
            }

            // Check that the first stopping point has a zero price
            if (i == 0
                    && point.prixDepuisDepart().compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException(
                        "The departure point price must be zero");
            }

            // Check that subsequent prices are strictly increasing
            if (previousPrice != null
                    && point.prixDepuisDepart().compareTo(previousPrice) <= 0) {
                throw new IllegalArgumentException(
                        "Stopping point prices must be strictly increasing");
            }

            previousTime = point.heurePassage();
            previousPrice = point.prixDepuisDepart();
        }
    }

    // Validate vehicle ownership
    private void validateVehicleOwnership(Vehicle vehicle, Driver driver) {

        if (!vehicle.getDriver().getId().equals(driver.getId())) {
            throw new IllegalArgumentException(
                    "This vehicle does not belong to the selected driver");
        }
    }

    // Validate vehicle capacity
    private void validateVehicleCapacity(Integer proposedSeats, Vehicle vehicle) {

        if (proposedSeats > vehicle.getCapacite()) {
            throw new IllegalArgumentException(
                    "Proposed seats exceed vehicle capacity");
        }
    }

    // Retrieve a driver by ID
    private Driver getDriverById(Long driverId) {

        return driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found"));
    }

    // Retrieve a vehicle by ID
    private Vehicle getVehicleById(Long vehicleId) {

        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vehicle not found"));
    }

    // Retrieve a trip entity by ID
    private Trip getTripEntity(Long id) {

        return tripRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found"));
    }

    // Get a trip by ID
    @Override
    @Transactional(readOnly = true)
    public TripResponseDTO getTripById(Long id) {

        Trip trip = getTripEntity(id);

        return tripMapper.toResponseDTO(trip);
    }

    // Get trips by driver ID
    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDTO> getTripsByDriverId(Long driverId) {

        // Check that the driver exists
        getDriverById(driverId);

        return tripRepository.findByDriverId(driverId)
                .stream()
                .map(tripMapper::toResponseDTO)
                .toList();
    }

    // Update a trip
    @Override
    public TripResponseDTO updateTrip(Long id, TripRequestDTO dto) {

        Trip trip = getTripEntity(id);

        // Prevent modifications to final trips
        if (trip.getStatut() != StatutTrajet.PUBLIE) {
            throw new IllegalStateException(
                    "Only unpublished trips can be published");
        }

        // Prevent updating trips that have confirmed reservations to avoid reservation point corruption
        boolean hasConfirmedReservations =
                !reservationRepository.findByTrajetIdAndStatut(
                        id, StatutReservation.CONFIRMEE
                ).isEmpty();

        if (hasConfirmedReservations) {
            throw new IllegalStateException(
                    "Cannot update a trip with confirmed reservations"
            );
        }

        // Validate stopping points
        validateStoppingPoints(dto.pointsArret());

        // Retrieve the driver and vehicle
        Driver driver = getDriverById(dto.driverId());
        Vehicle vehicle = getVehicleById(dto.vehicleId());

        // Validate vehicle ownership and capacity
        validateVehicleOwnership(vehicle, driver);
        validateVehicleCapacity(dto.placesProposees(), vehicle);

        // Update trip information
        trip.setDriver(driver);
        trip.setVehicle(vehicle);
        trip.setPlacesProposees(dto.placesProposees());

        // Clean up any past cancelled reservations to prevent FK constraint violations when replacing stopping points
        List<Reservation> pastReservations = reservationRepository.findByTrajetId(id);
        if (pastReservations != null && !pastReservations.isEmpty()) {
            reservationRepository.deleteAll(pastReservations);
        }

        // Replace existing stopping points
        trip.getPointsArret().clear();

        List<PointArret> points = dto.pointsArret().stream()
                .map(tripMapper::toEntity)
                .peek(point -> point.setTrajet(trip))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        trip.getPointsArret().addAll(points);

        return tripMapper.toResponseDTO(tripRepository.save(trip));
    }

    // Delete a trip
    @Override
    public void deleteTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        boolean hasConfirmedReservations =
                !reservationRepository.findByTrajetIdAndStatut(
                        id, StatutReservation.CONFIRMEE
                ).isEmpty();

        if (hasConfirmedReservations) {
            throw new IllegalStateException(
                    "Cannot delete a trip with confirmed reservations"
            );
        }

        // Clean up any past/cancelled reservations associated with this trip to prevent FK violation
        List<Reservation> pastReservations = reservationRepository.findByTrajetId(id);
        if (pastReservations != null && !pastReservations.isEmpty()) {
            reservationRepository.deleteAll(pastReservations);
        }

        tripRepository.delete(trip);
    }

    // Publish a trip
    @Override
    public TripResponseDTO publishTrip(Long id) {

        Trip trip = getTripEntity(id);

        if (trip.getStatut() == StatutTrajet.TERMINE
                || trip.getStatut() == StatutTrajet.ANNULE) {
            throw new IllegalStateException(
                    "A completed or cancelled trip cannot be published"
            );
        }

        trip.setStatut(StatutTrajet.PUBLIE);

        return tripMapper.toResponseDTO(trip);
    }


    // Complete a trip
    @Override
    public TripResponseDTO completeTrip(Long id) {

        Trip trip = getTripEntity(id);

        if (trip.getStatut() != StatutTrajet.PUBLIE
                && trip.getStatut() != StatutTrajet.COMPLET) {
            throw new IllegalStateException(
                    "Only published or full trips can be completed");
        }

        // Ensure departure time has passed
        LocalDateTime departureTime = trip.getPointsArret().stream()
                .min((a, b) -> Integer.compare(a.getOrdre(), b.getOrdre()))
                .orElseThrow(() ->
                        new IllegalStateException("Trip has no stopping points"))
                .getHeurePassage();

        if (LocalDateTime.now().isBefore(departureTime)) {
            throw new IllegalStateException(
                    "A trip cannot be completed before its departure time");
        }

        trip.setStatut(StatutTrajet.TERMINE);

        return tripMapper.toResponseDTO(trip);
    }

    // Cancel a trip
    // Cancel a trip
    @Override
    public TripResponseDTO cancelTrip(Long id) {

        Trip trip = getTripEntity(id);

        if (trip.getStatut() != StatutTrajet.PUBLIE
                && trip.getStatut() != StatutTrajet.COMPLET) {
            throw new IllegalStateException(
                    "Only published or full trips can be cancelled");
        }

        // Cancel all confirmed reservations
        List<Reservation> reservations =
                reservationRepository.findByTrajetIdAndStatut(
                        id,
                        StatutReservation.CONFIRMEE
                );

        reservations.forEach(reservation ->
                reservation.setStatut(StatutReservation.ANNULEE)
        );

        reservationRepository.saveAll(reservations);

        // Cancel the trip
        trip.setStatut(StatutTrajet.ANNULE);

        return tripMapper.toResponseDTO(trip);
    }
}
