package com.covoiturage.service.reservation;

import com.covoiturage.dto.request.ReservationRequestDTO;
import com.covoiturage.dto.response.ReservationResponseDTO;
import com.covoiturage.entity.PointArret;
import com.covoiturage.entity.Reservation;
import com.covoiturage.entity.Trip;
import com.covoiturage.entity.User;
import com.covoiturage.enums.StatutReservation;
import com.covoiturage.enums.StatutTrajet;
import com.covoiturage.enums.UserRole;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.repository.PointArretRepository;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.repository.TripRepository;
import com.covoiturage.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final PointArretRepository pointArretRepository;
    private final ReservationMapper reservationMapper;

    //get and validate passenger
    private User getPassenger(Long passengerId) {
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passenger not found with ID: " + passengerId
                ));

        if (passenger.getRole() != UserRole.PASSENGER) {
            throw new IllegalArgumentException(
                    "Only passengers can make reservations"
            );
        }

        return passenger;
    }

    // get trip
    private Trip getTrip(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trip not found with ID: " + tripId
                ));
    }
//get trip points
private PointArret getTripPoint(Long pointId, Long tripId) {
    return pointArretRepository.findByIdAndTrajetId(pointId, tripId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Stop point not found in this trip"
            ));
}

}