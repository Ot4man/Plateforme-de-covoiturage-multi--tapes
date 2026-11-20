package com.covoiturage.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import jakarta.validation.Valid;

public record TripRequestDTO(

        @NotNull
        Long driverId,

        @NotNull
        Long vehicleId,

        @NotNull
        @Min(1)
        Integer placesProposees,

        @NotNull
        @Valid
        List<PointArretRequestDTO> pointsArret

) {}
