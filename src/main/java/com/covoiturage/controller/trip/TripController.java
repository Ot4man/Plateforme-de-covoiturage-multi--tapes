package com.covoiturage.controller.trip;

import com.covoiturage.dto.request.TripRequestDTO;
import com.covoiturage.dto.response.TripResponseDTO;
import com.covoiturage.service.trip.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponseDTO> createTrip(
            @Valid @RequestBody TripRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripService.createTrip(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDTO> getTripById(
            @PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TripResponseDTO>> getTripsByDriverId(
            @PathVariable Long driverId) {
        return ResponseEntity.ok(
                tripService.getTripsByDriverId(driverId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponseDTO> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripRequestDTO dto) {
        return ResponseEntity.ok(tripService.updateTrip(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<TripResponseDTO> publishTrip(
            @PathVariable Long id) {
        return ResponseEntity.ok(tripService.publishTrip(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TripResponseDTO> completeTrip(
            @PathVariable Long id) {
        return ResponseEntity.ok(tripService.completeTrip(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TripResponseDTO> cancelTrip(
            @PathVariable Long id) {
        return ResponseEntity.ok(tripService.cancelTrip(id));
    }
}
