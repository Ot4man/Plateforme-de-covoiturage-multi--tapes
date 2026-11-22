package com.covoiturage.service.trip;

import com.covoiturage.dto.request.PointArretRequestDTO;
import com.covoiturage.dto.request.TripRequestDTO;
import com.covoiturage.dto.response.TripResponseDTO;
import com.covoiturage.entity.Driver;
import com.covoiturage.entity.PointArret;
import com.covoiturage.entity.Reservation;
import com.covoiturage.entity.Trip;
import com.covoiturage.entity.User;
import com.covoiturage.entity.Vehicle;
import com.covoiturage.enums.StatutReservation;
import com.covoiturage.enums.StatutTrajet;
import com.covoiturage.enums.UserRole;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.TripMapper;
import com.covoiturage.repository.DriverRepository;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.repository.TripRepository;
import com.covoiturage.repository.VehicleRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripServiceImpl Unit Tests")
class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private TripMapper tripMapper;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private TripServiceImpl tripService;

    private User driverUser;
    private Driver driver;
    private Vehicle vehicle;
    private List<PointArretRequestDTO> validPointsDTO;
    private TripRequestDTO validTripRequestDTO;

    @BeforeEach
    void setUp() {
        driverUser = new User();
        driverUser.setId(1L);
        driverUser.setFirstName("John");
        driverUser.setLastName("Doe");
        driverUser.setRole(UserRole.DRIVER);

        driver = new Driver();
        driver.setId(1L);
        driver.setUser(driverUser);
        driver.setLicenseNumber("LIC-12345");

        vehicle = new Vehicle();
        vehicle.setId(10L);
        vehicle.setMarque("Toyota");
        vehicle.setModele("Corolla");
        vehicle.setImmatriculation("AA-123-BB");
        vehicle.setCapacite(4);
        vehicle.setDriver(driver);

        LocalDateTime departureTime = LocalDateTime.now().plusDays(2);

        validPointsDTO = List.of(
                new PointArretRequestDTO("Paris", 0, departureTime, BigDecimal.ZERO),
                new PointArretRequestDTO("Lyon", 1, departureTime.plusHours(2), new BigDecimal("35.00"))
        );

        validTripRequestDTO = new TripRequestDTO(
                driver.getId(),
                vehicle.getId(),
                3,
                validPointsDTO
        );
    }

    private PointArret createPointArret(String ville, int ordre, LocalDateTime time, BigDecimal price, Trip trip) {
        PointArret point = new PointArret();
        point.setVille(ville);
        point.setOrdre(ordre);
        point.setHeurePassage(time);
        point.setPrixDepuisDepart(price);
        point.setTrajet(trip);
        return point;
    }

    @Nested
    @DisplayName("Successful Scenarios")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Create a trip with a valid driver and their own vehicle")
        void createTrip_WithValidDriverAndOwnVehicle_ShouldSucceed() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
            when(tripMapper.toEntity(any(PointArretRequestDTO.class))).thenAnswer(invocation -> {
                PointArretRequestDTO dto = invocation.getArgument(0);
                PointArret point = new PointArret();
                point.setVille(dto.ville());
                point.setOrdre(dto.ordre());
                point.setHeurePassage(dto.heurePassage());
                point.setPrixDepuisDepart(dto.prixDepuisDepart());
                return point;
            });

            Trip savedTrip = new Trip();
            savedTrip.setId(100L);
            savedTrip.setDriver(driver);
            savedTrip.setVehicle(vehicle);
            savedTrip.setPlacesProposees(3);
            savedTrip.setStatut(StatutTrajet.PUBLIE);

            when(tripRepository.save(any(Trip.class))).thenReturn(savedTrip);

            TripResponseDTO expectedResponse = new TripResponseDTO(
                    100L, driver.getId(), vehicle.getId(), 3,
                    LocalDateTime.now(), StatutTrajet.PUBLIE, Collections.emptyList()
            );
            when(tripMapper.toResponseDTO(savedTrip)).thenReturn(expectedResponse);

            // Act
            TripResponseDTO result = tripService.createTrip(validTripRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.statut()).isEqualTo(StatutTrajet.PUBLIE);

            ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
            verify(tripRepository).save(tripCaptor.capture());
            Trip capturedTrip = tripCaptor.getValue();
            assertThat(capturedTrip.getDriver()).isEqualTo(driver);
            assertThat(capturedTrip.getVehicle()).isEqualTo(vehicle);
            assertThat(capturedTrip.getPlacesProposees()).isEqualTo(3);
            assertThat(capturedTrip.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
            assertThat(capturedTrip.getPointsArret()).hasSize(2);
        }

        @Test
        @DisplayName("Retrieve a trip by ID")
        void getTripById_WhenTripExists_ShouldReturnTrip() {
            // Arrange
            Trip trip = new Trip();
            trip.setId(100L);
            trip.setStatut(StatutTrajet.PUBLIE);

            TripResponseDTO expectedResponse = new TripResponseDTO(
                    100L, driver.getId(), vehicle.getId(), 3,
                    LocalDateTime.now(), StatutTrajet.PUBLIE, Collections.emptyList()
            );

            when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
            when(tripMapper.toResponseDTO(trip)).thenReturn(expectedResponse);

            // Act
            TripResponseDTO result = tripService.getTripById(100L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(100L);
            verify(tripRepository).findById(100L);
        }

        @Test
        @DisplayName("Retrieve all trips belonging to a driver")
        void getTripsByDriverId_WhenDriverExists_ShouldReturnTripsList() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

            Trip trip1 = new Trip();
            trip1.setId(101L);
            Trip trip2 = new Trip();
            trip2.setId(102L);

            when(tripRepository.findByDriverId(driver.getId())).thenReturn(List.of(trip1, trip2));

            TripResponseDTO response1 = new TripResponseDTO(
                    101L, driver.getId(), vehicle.getId(), 3,
                    LocalDateTime.now(), StatutTrajet.PUBLIE, Collections.emptyList()
            );
            TripResponseDTO response2 = new TripResponseDTO(
                    102L, driver.getId(), vehicle.getId(), 2,
                    LocalDateTime.now(), StatutTrajet.PUBLIE, Collections.emptyList()
            );

            when(tripMapper.toResponseDTO(trip1)).thenReturn(response1);
            when(tripMapper.toResponseDTO(trip2)).thenReturn(response2);

            // Act
            List<TripResponseDTO> results = tripService.getTripsByDriverId(driver.getId());

            // Assert
            assertThat(results).hasSize(2);
            assertThat(results).extracting(TripResponseDTO::id).containsExactly(101L, 102L);
            verify(driverRepository).findById(driver.getId());
            verify(tripRepository).findByDriverId(driver.getId());
        }

        @Test
        @DisplayName("Update a published trip")
        void updateTrip_WhenPublishedTrip_ShouldUpdateAndReturnResponse() {
            // Arrange
            Trip existingTrip = new Trip();
            existingTrip.setId(100L);
            existingTrip.setStatut(StatutTrajet.PUBLIE);
            existingTrip.setDriver(driver);
            existingTrip.setVehicle(vehicle);
            existingTrip.setPlacesProposees(2);
            existingTrip.setPointsArret(new ArrayList<>());

            when(tripRepository.findById(100L)).thenReturn(Optional.of(existingTrip));
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
            when(tripMapper.toEntity(any(PointArretRequestDTO.class))).thenAnswer(invocation -> {
                PointArretRequestDTO dto = invocation.getArgument(0);
                PointArret point = new PointArret();
                point.setVille(dto.ville());
                point.setOrdre(dto.ordre());
                point.setHeurePassage(dto.heurePassage());
                point.setPrixDepuisDepart(dto.prixDepuisDepart());
                return point;
            });
            when(tripRepository.save(existingTrip)).thenReturn(existingTrip);

            TripResponseDTO updatedResponse = new TripResponseDTO(
                    100L, driver.getId(), vehicle.getId(), 3,
                    LocalDateTime.now(), StatutTrajet.PUBLIE, Collections.emptyList()
            );
            when(tripMapper.toResponseDTO(existingTrip)).thenReturn(updatedResponse);

            // Act
            TripResponseDTO result = tripService.updateTrip(100L, validTripRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(existingTrip.getPlacesProposees()).isEqualTo(3);
            assertThat(existingTrip.getPointsArret()).hasSize(2);
            verify(tripRepository).save(existingTrip);
        }

        @Test
        @DisplayName("Delete a trip without confirmed reservations")
        void deleteTrip_WhenNoConfirmedReservations_ShouldDeleteSuccessfully() {
            // Arrange
            Trip trip = new Trip();
            trip.setId(100L);

            when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
            when(reservationRepository.findByTrajetIdAndStatut(100L, StatutReservation.CONFIRMEE))
                    .thenReturn(Collections.emptyList());

            // Act
            tripService.deleteTrip(100L);

            // Assert
            verify(tripRepository).delete(trip);
        }

        @Test
        @DisplayName("Delete a trip with past cancelled reservations cleans up reservations and deletes trip")
        void deleteTrip_WhenTripHasPastCancelledReservations_ShouldCleanUpAndSucceed() {
            // Arrange
            Trip trip = new Trip();
            trip.setId(100L);

            when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
            when(reservationRepository.findByTrajetIdAndStatut(100L, StatutReservation.CONFIRMEE))
                    .thenReturn(Collections.emptyList());

            Reservation cancelledRes = new Reservation();
            cancelledRes.setId(1L);
            cancelledRes.setStatut(StatutReservation.ANNULEE);

            when(reservationRepository.findByTrajetId(100L))
                    .thenReturn(List.of(cancelledRes));

            // Act
            tripService.deleteTrip(100L);

            // Assert
            verify(reservationRepository).deleteAll(List.of(cancelledRes));
            verify(tripRepository).delete(trip);
        }

        @Test
        @DisplayName("Complete a trip after its departure time")
        void completeTrip_WhenAfterDepartureTime_ShouldSetStatusTermine() {
            // Arrange
            Trip trip = new Trip();
            trip.setId(100L);
            trip.setStatut(StatutTrajet.PUBLIE);

            LocalDateTime pastDeparture = LocalDateTime.now().minusHours(2);
            PointArret departurePoint = createPointArret("Paris", 0, pastDeparture, BigDecimal.ZERO, trip);
            PointArret arrivalPoint = createPointArret("Lyon", 1, pastDeparture.plusHours(2), new BigDecimal("30.00"), trip);
            trip.setPointsArret(new ArrayList<>(List.of(departurePoint, arrivalPoint)));

            when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));

            TripResponseDTO completedResponse = new TripResponseDTO(
                    100L, driver.getId(), vehicle.getId(), 3,
                    LocalDateTime.now(), StatutTrajet.TERMINE, Collections.emptyList()
            );
            when(tripMapper.toResponseDTO(trip)).thenReturn(completedResponse);

            // Act
            TripResponseDTO result = tripService.completeTrip(100L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(trip.getStatut()).isEqualTo(StatutTrajet.TERMINE);
            verify(tripMapper).toResponseDTO(trip);
        }

        @Test
        @DisplayName("Cancel a published trip")
        void cancelTrip_WhenPublishedTrip_ShouldSetStatusAnnule() {
            // Arrange
            Trip trip = new Trip();
            trip.setId(100L);
            trip.setStatut(StatutTrajet.PUBLIE);

            when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
            when(reservationRepository.findByTrajetIdAndStatut(100L, StatutReservation.CONFIRMEE))
                    .thenReturn(Collections.emptyList());

            TripResponseDTO cancelledResponse = new TripResponseDTO(
                    100L, driver.getId(), vehicle.getId(), 3,
                    LocalDateTime.now(), StatutTrajet.ANNULE, Collections.emptyList()
            );
            when(tripMapper.toResponseDTO(trip)).thenReturn(cancelledResponse);

            // Act
            TripResponseDTO result = tripService.cancelTrip(100L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(trip.getStatut()).isEqualTo(StatutTrajet.ANNULE);
            verify(reservationRepository).saveAll(Collections.emptyList());
        }

        @Test
        @DisplayName("Cancel a full trip and cancel all its confirmed reservations")
        void cancelTrip_WhenFullTripWithConfirmedReservations_ShouldCancelTripAndReservations() {
            // Arrange
            Trip trip = new Trip();
            trip.setId(100L);
            trip.setStatut(StatutTrajet.COMPLET);

            Reservation res1 = new Reservation();
            res1.setId(1L);
            res1.setStatut(StatutReservation.CONFIRMEE);

            Reservation res2 = new Reservation();
            res2.setId(2L);
            res2.setStatut(StatutReservation.CONFIRMEE);

            List<Reservation> confirmedReservations = List.of(res1, res2);

            when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
            when(reservationRepository.findByTrajetIdAndStatut(100L, StatutReservation.CONFIRMEE))
                    .thenReturn(confirmedReservations);

            TripResponseDTO responseDTO = new TripResponseDTO(
                    100L, driver.getId(), vehicle.getId(), 3,
                    LocalDateTime.now(), StatutTrajet.ANNULE, Collections.emptyList()
            );
            when(tripMapper.toResponseDTO(trip)).thenReturn(responseDTO);

            // Act
            tripService.cancelTrip(100L);

            // Assert
            assertThat(trip.getStatut()).isEqualTo(StatutTrajet.ANNULE);
            assertThat(res1.getStatut()).isEqualTo(StatutReservation.ANNULEE);
            assertThat(res2.getStatut()).isEqualTo(StatutReservation.ANNULEE);
            verify(reservationRepository).saveAll(confirmedReservations);
        }
    }

    @Nested
    @DisplayName("Rejected Scenarios")
    class RejectedScenarios {

        @Test
        @DisplayName("Create a trip with fewer than two stopping points")
        void createTrip_WhenFewerThanTwoStoppingPoints_ShouldThrowException() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

            List<PointArretRequestDTO> singlePoint = List.of(
                    new PointArretRequestDTO("Paris", 0, LocalDateTime.now().plusDays(1), BigDecimal.ZERO)
            );
            TripRequestDTO request = new TripRequestDTO(driver.getId(), vehicle.getId(), 3, singlePoint);

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A trip must contain at least two stopping points");
        }

        @Test
        @DisplayName("Create a trip with duplicate cities")
        void createTrip_WhenDuplicateCities_ShouldThrowException() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

            LocalDateTime time = LocalDateTime.now().plusDays(1);
            List<PointArretRequestDTO> duplicateCities = List.of(
                    new PointArretRequestDTO("Paris", 0, time, BigDecimal.ZERO),
                    new PointArretRequestDTO("paris", 1, time.plusHours(2), new BigDecimal("25.00"))
            );
            TripRequestDTO request = new TripRequestDTO(driver.getId(), vehicle.getId(), 3, duplicateCities);

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A city cannot appear more than once in a trip");
        }

        @Test
        @DisplayName("Create a trip with an invalid stopping point order")
        void createTrip_WhenInvalidStoppingPointOrder_ShouldThrowException() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

            LocalDateTime time = LocalDateTime.now().plusDays(1);
            List<PointArretRequestDTO> invalidOrder = List.of(
                    new PointArretRequestDTO("Paris", 1, time, BigDecimal.ZERO),
                    new PointArretRequestDTO("Lyon", 2, time.plusHours(2), new BigDecimal("25.00"))
            );
            TripRequestDTO request = new TripRequestDTO(driver.getId(), vehicle.getId(), 3, invalidOrder);

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Stopping point order must start at 0 and be consecutive");
        }

        @Test
        @DisplayName("Create a trip with departure time in the past")
        void createTrip_WhenDepartureTimeInPast_ShouldThrowException() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

            LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
            List<PointArretRequestDTO> pastDeparture = List.of(
                    new PointArretRequestDTO("Paris", 0, pastTime, BigDecimal.ZERO),
                    new PointArretRequestDTO("Lyon", 1, pastTime.plusHours(3), new BigDecimal("25.00"))
            );
            TripRequestDTO request = new TripRequestDTO(driver.getId(), vehicle.getId(), 3, pastDeparture);

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Departure time must be in the future");
        }

        @Test
        @DisplayName("Create a trip with stopping times that are not strictly increasing")
        void createTrip_WhenStoppingTimesNotStrictlyIncreasing_ShouldThrowException() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

            LocalDateTime time = LocalDateTime.now().plusDays(1);
            List<PointArretRequestDTO> nonIncreasingTimes = List.of(
                    new PointArretRequestDTO("Paris", 0, time, BigDecimal.ZERO),
                    new PointArretRequestDTO("Lyon", 1, time.minusHours(1), new BigDecimal("25.00"))
            );
            TripRequestDTO request = new TripRequestDTO(driver.getId(), vehicle.getId(), 3, nonIncreasingTimes);

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Stopping times must be strictly increasing");
        }

        @Test
        @DisplayName("Create a trip when the first point price is not zero")
        void createTrip_WhenFirstPointPriceNotZero_ShouldThrowException() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

            LocalDateTime time = LocalDateTime.now().plusDays(1);
            List<PointArretRequestDTO> nonZeroFirstPrice = List.of(
                    new PointArretRequestDTO("Paris", 0, time, new BigDecimal("10.00")),
                    new PointArretRequestDTO("Lyon", 1, time.plusHours(2), new BigDecimal("35.00"))
            );
            TripRequestDTO request = new TripRequestDTO(driver.getId(), vehicle.getId(), 3, nonZeroFirstPrice);

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The departure point price must be zero");
        }

        @Test
        @DisplayName("Create a trip with non-increasing cumulative prices")
        void createTrip_WhenNonIncreasingCumulativePrices_ShouldThrowException() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

            LocalDateTime time = LocalDateTime.now().plusDays(1);
            List<PointArretRequestDTO> decreasingPrices = List.of(
                    new PointArretRequestDTO("Paris", 0, time, BigDecimal.ZERO),
                    new PointArretRequestDTO("Lyon", 1, time.plusHours(2), BigDecimal.ZERO)
            );
            TripRequestDTO request = new TripRequestDTO(driver.getId(), vehicle.getId(), 3, decreasingPrices);

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Stopping point prices must be strictly increasing");
        }

        @Test
        @DisplayName("Create a trip with more proposed seats than vehicle capacity")
        void createTrip_WhenProposedSeatsExceedVehicleCapacity_ShouldThrowException() {
            // Arrange
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

            // Vehicle capacity is 4, requesting 5 seats
            TripRequestDTO request = new TripRequestDTO(driver.getId(), vehicle.getId(), 5, validPointsDTO);

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Proposed seats exceed vehicle capacity");
        }

        @Test
        @DisplayName("Create a trip using another driver's vehicle")
        void createTrip_WhenVehicleBelongsToAnotherDriver_ShouldThrowException() {
            // Arrange
            Driver anotherDriver = new Driver();
            anotherDriver.setId(99L);
            vehicle.setDriver(anotherDriver);

            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
            when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(validTripRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("This vehicle does not belong to the selected driver");
        }

        @Test
        @DisplayName("Reject trip creation when the associated user does not have the DRIVER role")
        void createTrip_WhenUserDoesNotHaveDriverRole_ShouldThrowException() {
            // Arrange
            driverUser.setRole(UserRole.PASSENGER);
            when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

            // Act & Assert
            assertThatThrownBy(() -> tripService.createTrip(validTripRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Only users with DRIVER role can create trips");
        }

        @Test
        @DisplayName("Reject updating a final trip")
        void updateTrip_WhenTripIsNotPublished_ShouldThrowException() {
            // Arrange
            Trip cancelledTrip = new Trip();
            cancelledTrip.setId(100L);
            cancelledTrip.setStatut(StatutTrajet.ANNULE);

            when(tripRepository.findById(100L)).thenReturn(Optional.of(cancelledTrip));

            // Act & Assert
            assertThatThrownBy(() -> tripService.updateTrip(100L, validTripRequestDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Only unpublished trips can be published");
        }

        @Test
        @DisplayName("Reject updating a trip with confirmed reservations to prevent point corruption")
        void updateTrip_WhenTripHasConfirmedReservations_ShouldThrowException() {
            // Arrange
            Trip existingTrip = new Trip();
            existingTrip.setId(100L);
            existingTrip.setStatut(StatutTrajet.PUBLIE);

            when(tripRepository.findById(100L)).thenReturn(Optional.of(existingTrip));
            when(reservationRepository.findByTrajetIdAndStatut(100L, StatutReservation.CONFIRMEE))
                    .thenReturn(List.of(new Reservation()));

            // Act & Assert
            assertThatThrownBy(() -> tripService.updateTrip(100L, validTripRequestDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot update a trip with confirmed reservations");
        }

        @Test
        @DisplayName("Reject deleting a trip with confirmed reservations")
        void deleteTrip_WhenTripHasConfirmedReservations_ShouldThrowException() {
            // Arrange
            Trip trip = new Trip();
            trip.setId(100L);

            when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
            when(reservationRepository.findByTrajetIdAndStatut(100L, StatutReservation.CONFIRMEE))
                    .thenReturn(List.of(new Reservation()));

            // Act & Assert
            assertThatThrownBy(() -> tripService.deleteTrip(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot delete a trip with confirmed reservations");
        }

        @Test
        @DisplayName("Reject completing a trip before departure")
        void completeTrip_WhenBeforeDepartureTime_ShouldThrowException() {
            // Arrange
            Trip trip = new Trip();
            trip.setId(100L);
            trip.setStatut(StatutTrajet.PUBLIE);

            LocalDateTime futureDeparture = LocalDateTime.now().plusHours(3);
            PointArret departurePoint = createPointArret("Paris", 0, futureDeparture, BigDecimal.ZERO, trip);
            trip.setPointsArret(new ArrayList<>(List.of(departurePoint)));

            when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));

            // Act & Assert
            assertThatThrownBy(() -> tripService.completeTrip(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("A trip cannot be completed before its departure time");
        }

        @Test
        @DisplayName("Reject cancelling a completed or already cancelled trip")
        void cancelTrip_WhenTripAlreadyCompletedOrCancelled_ShouldThrowException() {
            // Arrange
            Trip completedTrip = new Trip();
            completedTrip.setId(100L);
            completedTrip.setStatut(StatutTrajet.TERMINE);

            when(tripRepository.findById(100L)).thenReturn(Optional.of(completedTrip));

            // Act & Assert
            assertThatThrownBy(() -> tripService.cancelTrip(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Only published or full trips can be cancelled");
        }
    }
}
