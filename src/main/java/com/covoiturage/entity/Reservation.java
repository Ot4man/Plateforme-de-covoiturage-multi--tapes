package com.covoiturage.entity;

import com.covoiturage.enums.StatutReservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passager;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trajet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pickup_point_id", nullable = false)
    private PointArret pointMontee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dropoff_point_id", nullable = false)
    private PointArret pointDescente;

    @Column(nullable = false)
    private Integer nombrePlaces;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(nullable = false)
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation statut;

    @PrePersist
    protected void onCreate() {
        dateReservation = LocalDateTime.now();

        if (statut == null) {
            statut = StatutReservation.CONFIRMEE;
        }
    }
}
