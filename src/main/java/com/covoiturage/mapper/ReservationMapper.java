package com.covoiturage.mapper;

import com.covoiturage.dto.response.ReservationResponseDTO;
import com.covoiturage.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "passager.id", target = "passagerId")
    @Mapping(source = "trajet.id", target = "trajetId")
    @Mapping(source = "pointMontee.ville", target = "villeMontee")
    @Mapping(source = "pointDescente.ville", target = "villeDescente")
    ReservationResponseDTO toResponseDTO(Reservation reservation);
}
