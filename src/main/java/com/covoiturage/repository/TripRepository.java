
package com.covoiturage.repository;

import com.covoiturage.entity.Trip;
import com.covoiturage.enums.StatutTrajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByDriverId(Long driverId);

    boolean existsByVehicleId(Long vehicleId);

    @Query("""
        SELECT DISTINCT t
        FROM Trip t
        JOIN t.pointsArret departure
        JOIN t.pointsArret arrival
        WHERE LOWER(departure.ville) = LOWER(:villeDepart)
        AND LOWER(arrival.ville) = LOWER(:villeArrivee)
        AND departure.ordre < arrival.ordre
        AND (:dateDepart IS NULL OR departure.heurePassage >= :dateDepart)
        AND (:prixMaximum IS NULL OR
             arrival.prixDepuisDepart - departure.prixDepuisDepart <= :prixMaximum)
        AND t.statut = :statut
    """)
    List<Trip> searchTrips(
            @Param("villeDepart") String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("dateDepart") LocalDateTime dateDepart,
            @Param("prixMaximum") BigDecimal prixMaximum,
            @Param("statut") StatutTrajet statut
    );
}
