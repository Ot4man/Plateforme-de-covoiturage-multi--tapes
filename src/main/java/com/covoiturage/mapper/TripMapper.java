package com.covoiturage.mapper;

import com.covoiturage.dto.request.PointArretRequestDTO;
import com.covoiturage.dto.response.TripResponseDTO;
import com.covoiturage.dto.response.PointArretResponseDTO;
import com.covoiturage.dto.response.TripSearchResponseDTO;
import com.covoiturage.entity.Trip;
import com.covoiturage.entity.PointArret;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripMapper {

    // Convert Trip entity to TripResponseDTO
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    TripResponseDTO toResponseDTO(Trip trip);

    // Convert PointArret entity to PointArretResponseDTO
    PointArretResponseDTO toResponseDTO(PointArret pointArret);

    // Convert PointArretRequestDTO to PointArret entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trajet", ignore = true)
    PointArret toEntity(PointArretRequestDTO dto);

    // Convert trip search results to TripSearchResponseDTO
    @Mapping(target = "trajetId", source = "trip.id")
    @Mapping(target = "conducteurId", source = "trip.driver.id")
    @Mapping(target = "vehiculeId", source = "trip.vehicle.id")
    @Mapping(target = "villeDepart", source = "departure.ville")
    @Mapping(target = "villeArrivee", source = "arrival.ville")
    @Mapping(target = "heureDepart", source = "departure.heurePassage")
    @Mapping(
            target = "prixParPlace",
            expression = "java(arrival.getPrixDepuisDepart().subtract(departure.getPrixDepuisDepart()))"
    )
    @Mapping(
            target = "placesDisponibles",
            expression = "java(trip.getPlacesProposees() - reservedSeats.intValue())"
    )
    TripSearchResponseDTO toSearchResponseDTO(
            Trip trip,
            PointArret departure,
            PointArret arrival,
            Long reservedSeats
    );
}
