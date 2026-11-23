
package com.covoiturage.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripSearchResponseDTO(
        Long trajetId,
        Long conducteurId,
        Long vehiculeId,
        String villeDepart,
        String villeArrivee,
        LocalDateTime heureDepart,
        BigDecimal prixParPlace,
        Integer placesDisponibles
) {}
