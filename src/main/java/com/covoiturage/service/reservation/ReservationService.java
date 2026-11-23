package com.covoiturage.service.reservation;

import com.covoiturage.dto.request.ReservationRequestDTO;
import com.covoiturage.dto.response.ReservationResponseDTO;
import java.util.List;
public interface ReservationService {
    ReservationResponseDTO createReservation(ReservationRequestDTO reservationRequestDTO);
    ReservationResponseDTO getReservationById(Long id);
    List<ReservationResponseDTO> getReservationsByPassengerId(Long passagerId);
    void cancelReservationById(Long id);
}
