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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationServiceImpl Unit Tests")
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private PointArretRepository pointArretRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User passenger;
    private Trip trip;
    private PointArret pickupPoint;
    private PointArret dropoffPoint;
    private ReservationRequestDTO validReservationRequestDTO;

    @BeforeEach
    void setUp() {
        passenger = new User();
        passenger.setId(1L);
        passenger.setFirstName("Alice");
        passenger.setLastName("Smith");
        passenger.setRole(UserRole.PASSENGER);

        trip = new Trip();
        trip.setId(10L);
        trip.setPlacesProposees(4);
        trip.setStatut(StatutTrajet.PUBLIE);
        trip.setDateCreation(LocalDateTime.now().minusDays(1));

        LocalDateTime departureTime = LocalDateTime.now().plusDays(3);

        pickupPoint = new PointArret();
        pickupPoint.setId(100L);
        pickupPoint.setVille("Paris");
        pickupPoint.setOrdre(0);
        pickupPoint.setHeurePassage(departureTime);
        pickupPoint.setPrixDepuisDepart(BigDecimal.ZERO);
        pickupPoint.setTrajet(trip);

        dropoffPoint = new PointArret();
        dropoffPoint.setId(200L);
        dropoffPoint.setVille("Lyon");
        dropoffPoint.setOrdre(1);
        dropoffPoint.setHeurePassage(departureTime.plusHours(2));
        dropoffPoint.setPrixDepuisDepart(new BigDecimal("30.00"));
        dropoffPoint.setTrajet(trip);

        trip.setPointsArret(new ArrayList<>(List.of(pickupPoint, dropoffPoint)));

        validReservationRequestDTO = new ReservationRequestDTO(
                passenger.getId(),
                trip.getId(),
                pickupPoint.getId(),
                dropoffPoint.getId(),
                2
        );
    }

    @Nested
    @DisplayName("Successful Scenarios")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Create a reservation with valid information")
        void createReservation_WithValidInformation_ShouldSucceed() {
            // Arrange
            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(pickupPoint, dropoffPoint));
            when(pointArretRepository.findByIdAndTrajetId(pickupPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(pickupPoint));
            when(pointArretRepository.findByIdAndTrajetId(dropoffPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(dropoffPoint));
            when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                    passenger.getId(), trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(false);
            when(reservationRepository.sumReservedSeats(trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(0L);

            Reservation savedReservation = new Reservation();
            savedReservation.setId(50L);
            savedReservation.setPassager(passenger);
            savedReservation.setTrajet(trip);
            savedReservation.setPointMontee(pickupPoint);
            savedReservation.setPointDescente(dropoffPoint);
            savedReservation.setNombrePlaces(2);
            savedReservation.setMontantTotal(new BigDecimal("60.00"));
            savedReservation.setStatut(StatutReservation.CONFIRMEE);

            when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

            ReservationResponseDTO expectedResponse = new ReservationResponseDTO(
                    50L, passenger.getId(), trip.getId(), "Paris", "Lyon",
                    2, new BigDecimal("60.00"), LocalDateTime.now(), StatutReservation.CONFIRMEE
            );
            when(reservationMapper.toResponseDTO(savedReservation)).thenReturn(expectedResponse);

            // Act
            ReservationResponseDTO result = reservationService.createReservation(validReservationRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(50L);
            assertThat(result.statut()).isEqualTo(StatutReservation.CONFIRMEE);

            ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(reservationCaptor.capture());
            Reservation captured = reservationCaptor.getValue();
            assertThat(captured.getPassager()).isEqualTo(passenger);
            assertThat(captured.getTrajet()).isEqualTo(trip);
            assertThat(captured.getPointMontee()).isEqualTo(pickupPoint);
            assertThat(captured.getPointDescente()).isEqualTo(dropoffPoint);
            assertThat(captured.getNombrePlaces()).isEqualTo(2);
            assertThat(captured.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
        }

        @Test
        @DisplayName("Calculate the reservation amount correctly")
        void createReservation_ShouldCalculateReservationAmountCorrectly() {
            // Arrange: segment price = 45.00 - 15.00 = 30.00, seats = 3 => total = 90.00
            pickupPoint.setPrixDepuisDepart(new BigDecimal("15.00"));
            dropoffPoint.setPrixDepuisDepart(new BigDecimal("45.00"));

            ReservationRequestDTO request = new ReservationRequestDTO(
                    passenger.getId(),
                    trip.getId(),
                    pickupPoint.getId(),
                    dropoffPoint.getId(),
                    3
            );

            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(pickupPoint, dropoffPoint));
            when(pointArretRepository.findByIdAndTrajetId(pickupPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(pickupPoint));
            when(pointArretRepository.findByIdAndTrajetId(dropoffPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(dropoffPoint));
            when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                    passenger.getId(), trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(false);
            when(reservationRepository.sumReservedSeats(trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(0L);

            when(reservationRepository.save(any(Reservation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            reservationService.createReservation(request);

            // Assert
            ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(captor.capture());
            Reservation captured = captor.getValue();

            BigDecimal expectedTotal = new BigDecimal("90.00").setScale(2, RoundingMode.HALF_UP);
            assertThat(captured.getMontantTotal()).isEqualByComparingTo(expectedTotal);
            assertThat(captured.getMontantTotal().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Automatically change trip status to COMPLET when all seats are reserved")
        void createReservation_WhenAllSeatsReserved_ShouldUpdateTripStatusToComplet() {
            // Arrange: Trip proposed seats = 2, reserving 2 seats
            trip.setPlacesProposees(2);
            ReservationRequestDTO request = new ReservationRequestDTO(
                    passenger.getId(),
                    trip.getId(),
                    pickupPoint.getId(),
                    dropoffPoint.getId(),
                    2
            );

            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(pickupPoint, dropoffPoint));
            when(pointArretRepository.findByIdAndTrajetId(pickupPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(pickupPoint));
            when(pointArretRepository.findByIdAndTrajetId(dropoffPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(dropoffPoint));
            when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                    passenger.getId(), trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(false);

            // First call in validateAvailableSeats returns 0L, second call in updateTripStatus returns 2L
            when(reservationRepository.sumReservedSeats(trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(0L)
                    .thenReturn(2L);

            when(reservationRepository.save(any(Reservation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            reservationService.createReservation(request);

            // Assert
            assertThat(trip.getStatut()).isEqualTo(StatutTrajet.COMPLET);
            verify(tripRepository).save(trip);
        }

        @Test
        @DisplayName("Retrieve a reservation by ID")
        void getReservationById_WhenExists_ShouldReturnReservation() {
            // Arrange
            Reservation reservation = new Reservation();
            reservation.setId(50L);

            ReservationResponseDTO expectedResponse = new ReservationResponseDTO(
                    50L, passenger.getId(), trip.getId(), "Paris", "Lyon",
                    2, new BigDecimal("60.00"), LocalDateTime.now(), StatutReservation.CONFIRMEE
            );

            when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
            when(reservationMapper.toResponseDTO(reservation)).thenReturn(expectedResponse);

            // Act
            ReservationResponseDTO result = reservationService.getReservationById(50L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(50L);
            verify(reservationRepository).findById(50L);
        }

        @Test
        @DisplayName("Retrieve reservations belonging to a passenger")
        void getReservationsByPassengerId_WhenPassengerHasReservations_ShouldReturnList() {
            // Arrange
            Reservation res1 = new Reservation();
            res1.setId(51L);
            Reservation res2 = new Reservation();
            res2.setId(52L);

            when(reservationRepository.findByPassager_Id(passenger.getId()))
                    .thenReturn(List.of(res1, res2));

            ReservationResponseDTO dto1 = new ReservationResponseDTO(
                    51L, passenger.getId(), trip.getId(), "Paris", "Lyon",
                    1, new BigDecimal("30.00"), LocalDateTime.now(), StatutReservation.CONFIRMEE
            );
            ReservationResponseDTO dto2 = new ReservationResponseDTO(
                    52L, passenger.getId(), trip.getId(), "Paris", "Lyon",
                    2, new BigDecimal("60.00"), LocalDateTime.now(), StatutReservation.CONFIRMEE
            );

            when(reservationMapper.toResponseDTO(res1)).thenReturn(dto1);
            when(reservationMapper.toResponseDTO(res2)).thenReturn(dto2);

            // Act
            List<ReservationResponseDTO> results =
                    reservationService.getReservationsByPassengerId(passenger.getId());

            // Assert
            assertThat(results).hasSize(2);
            assertThat(results).extracting(ReservationResponseDTO::id).containsExactly(51L, 52L);
            verify(reservationRepository).findByPassager_Id(passenger.getId());
        }

        @Test
        @DisplayName("Cancel a confirmed reservation more than 24 hours before departure")
        void cancelReservation_MoreThan24HoursBeforeDeparture_ShouldCancelSuccessfully() {
            // Arrange
            Reservation reservation = new Reservation();
            reservation.setId(50L);
            reservation.setStatut(StatutReservation.CONFIRMEE);
            reservation.setTrajet(trip);

            when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

            // Act
            reservationService.cancelReservation(50L);

            // Assert
            assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        }

        @Test
        @DisplayName("Change trip status from COMPLET to PUBLIE when cancellation frees a seat")
        void cancelReservation_WhenTripWasFull_ShouldChangeTripStatusFromCompletToPublie() {
            // Arrange
            trip.setStatut(StatutTrajet.COMPLET);

            Reservation reservation = new Reservation();
            reservation.setId(50L);
            reservation.setStatut(StatutReservation.CONFIRMEE);
            reservation.setTrajet(trip);

            when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

            // Act
            reservationService.cancelReservation(50L);

            // Assert
            assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ANNULEE);
            assertThat(trip.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
            verify(tripRepository).save(trip);
        }

        @Test
        @DisplayName("Ensure cancelled reservations are not counted as occupied seats")
        void validateAvailableSeats_CancelledReservationsShouldNotCountTowardOccupiedSeats() {
            // Arrange: sumReservedSeats queries ONLY CONFIRMEE status, ignoring ANNULEE
            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(pickupPoint, dropoffPoint));
            when(pointArretRepository.findByIdAndTrajetId(pickupPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(pickupPoint));
            when(pointArretRepository.findByIdAndTrajetId(dropoffPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(dropoffPoint));
            when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                    passenger.getId(), trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(false);

            // 0 confirmed seats occupied even if cancelled reservations exist in the database
            when(reservationRepository.sumReservedSeats(trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(0L);

            ReservationRequestDTO requestAllSeats = new ReservationRequestDTO(
                    passenger.getId(),
                    trip.getId(),
                    pickupPoint.getId(),
                    dropoffPoint.getId(),
                    4 // full capacity requested
            );

            when(reservationRepository.save(any(Reservation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            reservationService.createReservation(requestAllSeats);

            // Assert: verify available seats check passed with all 4 seats available
            verify(reservationRepository, atLeastOnce()).sumReservedSeats(trip.getId(), StatutReservation.CONFIRMEE);
            verify(reservationRepository).save(any(Reservation.class));
        }
    }

    @Nested
    @DisplayName("Rejected Scenarios")
    class RejectedScenarios {

        @Test
        @DisplayName("Reject reservation when the passenger does not exist")
        void createReservation_WhenPassengerNotFound_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(passenger.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservationService.createReservation(validReservationRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Passenger not found");
        }

        @Test
        @DisplayName("Reject reservation when the user is not a PASSENGER")
        void createReservation_WhenUserIsNotPassenger_ShouldThrowException() {
            // Arrange
            passenger.setRole(UserRole.DRIVER);
            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));

            // Act & Assert
            assertThatThrownBy(() -> reservationService.createReservation(validReservationRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Only passengers can make reservations");
        }

        @Test
        @DisplayName("Reject reservation on a non-PUBLIE trip")
        void createReservation_WhenTripNotPublished_ShouldThrowException() {
            // Arrange
            trip.setStatut(StatutTrajet.COMPLET);
            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));

            // Act & Assert
            assertThatThrownBy(() -> reservationService.createReservation(validReservationRequestDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You can only reserve seats on published trips");
        }

        @Test
        @DisplayName("Reject reservation when the trip departure time has passed")
        void createReservation_WhenTripDepartureTimeHasPassed_ShouldThrowException() {
            // Arrange
            LocalDateTime pastDeparture = LocalDateTime.now().minusHours(1);
            PointArret pastPickup = new PointArret();
            pastPickup.setOrdre(0);
            pastPickup.setHeurePassage(pastDeparture);

            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(pastPickup));

            // Act & Assert
            assertThatThrownBy(() -> reservationService.createReservation(validReservationRequestDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You cannot reserve a trip that has already started");
        }

        @Test
        @DisplayName("Reject pickup point that does not belong to the selected trip")
        void createReservation_WhenPickupPointDoesNotBelongToTrip_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(pickupPoint, dropoffPoint));
            when(pointArretRepository.findByIdAndTrajetId(pickupPoint.getId(), trip.getId()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservationService.createReservation(validReservationRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Point does not belong to this trip");
        }

        @Test
        @DisplayName("Reject drop-off point that does not belong to the selected trip")
        void createReservation_WhenDropoffPointDoesNotBelongToTrip_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(pickupPoint, dropoffPoint));
            when(pointArretRepository.findByIdAndTrajetId(pickupPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(pickupPoint));
            when(pointArretRepository.findByIdAndTrajetId(dropoffPoint.getId(), trip.getId()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservationService.createReservation(validReservationRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Point does not belong to this trip");
        }

        @Test
        @DisplayName("Reject pickup after or equal to drop-off")
        void createReservation_WhenPickupAfterOrEqualDropoff_ShouldThrowException() {
            // Arrange: pickup has order 1, dropoff has order 0
            pickupPoint.setOrdre(1);
            dropoffPoint.setOrdre(0);

            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(dropoffPoint, pickupPoint));
            when(pointArretRepository.findByIdAndTrajetId(pickupPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(pickupPoint));
            when(pointArretRepository.findByIdAndTrajetId(dropoffPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(dropoffPoint));

            // Act & Assert
            assertThatThrownBy(() -> reservationService.createReservation(validReservationRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Pickup point must be before dropoff point");
        }

        @Test
        @DisplayName("Reject duplicate confirmed reservations by the same passenger on the same trip")
        void createReservation_WhenDuplicateConfirmedReservation_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(pickupPoint, dropoffPoint));
            when(pointArretRepository.findByIdAndTrajetId(pickupPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(pickupPoint));
            when(pointArretRepository.findByIdAndTrajetId(dropoffPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(dropoffPoint));
            when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                    passenger.getId(), trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> reservationService.createReservation(validReservationRequestDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You already have a confirmed reservation for this trip");
        }

        @Test
        @DisplayName("Reject reservations exceeding available seats")
        void createReservation_WhenExceedingAvailableSeats_ShouldThrowException() {
            // Arrange: proposed seats = 4, reserved seats = 3 => available = 1, requesting 2
            when(userRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
            when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
            when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(trip.getId()))
                    .thenReturn(List.of(pickupPoint, dropoffPoint));
            when(pointArretRepository.findByIdAndTrajetId(pickupPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(pickupPoint));
            when(pointArretRepository.findByIdAndTrajetId(dropoffPoint.getId(), trip.getId()))
                    .thenReturn(Optional.of(dropoffPoint));
            when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                    passenger.getId(), trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(false);
            when(reservationRepository.sumReservedSeats(trip.getId(), StatutReservation.CONFIRMEE))
                    .thenReturn(3L);

            // Act & Assert
            assertThatThrownBy(() -> reservationService.createReservation(validReservationRequestDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Not enough available seats");
        }

        @Test
        @DisplayName("Reject cancellation of an already cancelled reservation")
        void cancelReservation_WhenReservationAlreadyCancelled_ShouldThrowException() {
            // Arrange
            Reservation reservation = new Reservation();
            reservation.setId(50L);
            reservation.setStatut(StatutReservation.ANNULEE);

            when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

            // Act & Assert
            assertThatThrownBy(() -> reservationService.cancelReservation(50L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Only confirmed reservations can be cancelled");
        }

        @Test
        @DisplayName("Reject cancellation less than 24 hours before departure")
        void cancelReservation_WhenLessThan24HoursBeforeDeparture_ShouldThrowException() {
            // Arrange: departure time in 12 hours (< 24h)
            LocalDateTime nearDeparture = LocalDateTime.now().plusHours(12);
            pickupPoint.setHeurePassage(nearDeparture);

            Reservation reservation = new Reservation();
            reservation.setId(50L);
            reservation.setStatut(StatutReservation.CONFIRMEE);
            reservation.setTrajet(trip);

            when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

            // Act & Assert
            assertThatThrownBy(() -> reservationService.cancelReservation(50L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Reservations can only be cancelled at least 24 hours before departure");
        }
    }
}
