
package com.covoiturage.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripSearchRequestDTO(

        @NotBlank
        String villeDepart,

        @NotBlank
        String villeArrivee,

        LocalDateTime dateDepart,

        @Min(1)
        Integer nombrePlaces,

        @DecimalMin("0.0")
        BigDecimal prixMaximum

) {}
