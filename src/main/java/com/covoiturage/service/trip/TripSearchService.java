package com.covoiturage.service.trip;

import com.covoiturage.dto.request.TripSearchRequestDTO;
import com.covoiturage.dto.response.TripSearchResponseDTO;
import com.covoiturage.entity.PointArret;
import com.covoiturage.entity.Trip;
import com.covoiturage.enums.StatutReservation;
import com.covoiturage.enums.StatutTrajet;
import com.covoiturage.mapper.TripMapper;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripSearchService {

    private final TripRepository tripRepository;
    private final ReservationRepository reservationRepository;
    private final TripMapper tripMapper;

    public List<TripSearchResponseDTO> searchTrips(TripSearchRequestDTO request) {

        List<Trip> trips = tripRepository.searchTrips(
                request.villeDepart(),
                request.villeArrivee(),
                request.dateDepart(),
                request.prixMaximum(),
                StatutTrajet.PUBLIE
        );

        if (trips.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tripIds = trips.stream()
                .map(Trip::getId)
                .toList();

        // Single batch query for all trips instead of N queries in the loop
        Map<Long, Long> reservedSeatsMap = reservationRepository
                .sumReservedSeatsByTripIds(tripIds, StatutReservation.CONFIRMEE)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));

        int requiredSeats = request.nombrePlaces() == null
                ? 1
                : request.nombrePlaces();

        List<TripSearchResponseDTO> results = new ArrayList<>();

        for (Trip trip : trips) {

            Long reservedSeats = reservedSeatsMap.getOrDefault(trip.getId(), 0L);

            int availableSeats =
                    trip.getPlacesProposees() - reservedSeats.intValue();

            if (availableSeats < requiredSeats) {
                continue;
            }

            PointArret departure = trip.getPointsArret().stream()
                    .filter(p -> p.getVille().equalsIgnoreCase(request.villeDepart()))
                    .findFirst()
                    .orElse(null);

            PointArret arrival = trip.getPointsArret().stream()
                    .filter(p -> p.getVille().equalsIgnoreCase(request.villeArrivee()))
                    .findFirst()
                    .orElse(null);

            if (departure == null || arrival == null) {
                continue;
            }

            TripSearchResponseDTO result = tripMapper.toSearchResponseDTO(
                    trip, departure, arrival, reservedSeats
            );

            results.add(result);
        }

        return results;
    }
}
