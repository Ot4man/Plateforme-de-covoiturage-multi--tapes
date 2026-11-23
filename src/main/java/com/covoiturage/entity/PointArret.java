
package com.covoiturage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_arret")
@Getter
@Setter
@NoArgsConstructor
public class PointArret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ville;

    @Column(nullable = false)
    private Integer ordre;

    @Column(nullable = false)
    private LocalDateTime heurePassage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixDepuisDepart;

    // Plusieurs points d'arrêt appartiennent à un trajet
    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trajet;
}
