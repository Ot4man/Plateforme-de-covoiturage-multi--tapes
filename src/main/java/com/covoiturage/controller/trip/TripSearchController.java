package com.covoiturage.controller.trip;

import com.covoiturage.dto.request.TripSearchRequestDTO;
import com.covoiturage.dto.response.TripSearchResponseDTO;
import com.covoiturage.service.trip.TripSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripSearchController {

    private final TripSearchService tripSearchService;

    @GetMapping("/search")
    public List<TripSearchResponseDTO> searchTrips(
            @Valid @ModelAttribute TripSearchRequestDTO request) {

        return tripSearchService.searchTrips(request);
    }
}
