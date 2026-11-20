package com.covoiturage.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretResponseDTO(
        Long id,
        String ville,
        Integer ordre,
        LocalDateTime heurePassage,
        BigDecimal prixDepuisDepart
) {}
