
package com.covoiturage.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretRequestDTO(

        @NotBlank
        String ville,

        @NotNull
        @Min(0)
        Integer ordre,

        @NotNull
        LocalDateTime heurePassage,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal prixDepuisDepart

) {}
