package com.covoiturage.controller.reservation;

import com.covoiturage.dto.request.ReservationRequestDTO;
import com.covoiturage.dto.response.ReservationResponseDTO;
import com.covoiturage.service.reservation.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDTO createReservation(
            @Valid @RequestBody ReservationRequestDTO dto) {
        return reservationService.createReservation(dto);
    }

    @GetMapping("/{id}")
    public ReservationResponseDTO getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @GetMapping("/passenger/{passagerId}")
    public List<ReservationResponseDTO> getReservationsByPassengerId(
            @PathVariable Long passagerId) {
        return reservationService.getReservationsByPassengerId(passagerId);
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
    }
}
