
package com.covoiturage.dto.response;
import com.covoiturage.enums.StatutTrajet;

import java.time.LocalDateTime;
import java.util.List;

public record TripResponseDTO(
        Long id,
        Long driverId,
        Long vehicleId,
        Integer placesProposees,
        LocalDateTime dateCreation,
        StatutTrajet statut,
        List<PointArretResponseDTO> pointsArret
) {}
