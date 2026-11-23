package com.covoiturage.repository;

import com.covoiturage.entity.Reservation;
import com.covoiturage.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTrajetId(Long trajetId);

    List<Reservation> findByPassagerId(Long passagerId);

    boolean existsByPassagerIdAndTrajetIdAndStatut(
            Long passagerId,
            Long trajetId,
            StatutReservation statut
    );

    List<Reservation> findByTrajetIdAndStatut(
            Long trajetId,
            StatutReservation statut
    );
    @Query("""
    SELECT COALESCE(SUM(r.nombrePlaces), 0)
    FROM Reservation r
    WHERE r.trajet.id = :tripId
    AND r.statut = :statut
""")
    Integer sumReservedSeats(
            @Param("tripId") Long tripId,
            @Param("statut") StatutReservation statut
    );
}
