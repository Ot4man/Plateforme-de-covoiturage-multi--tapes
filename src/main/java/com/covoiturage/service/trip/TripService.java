
package com.covoiturage.service.trip;

import com.covoiturage.dto.request.TripRequestDTO;
import com.covoiturage.dto.response.TripResponseDTO;

import java.util.List;

public interface TripService {

    // Create a trip
    TripResponseDTO createTrip(TripRequestDTO dto);

    // Get a trip by ID
    TripResponseDTO getTripById(Long id);

    // Get trips by driver ID
    List<TripResponseDTO> getTripsByDriverId(Long driverId);

    // Update a trip
    TripResponseDTO updateTrip(Long id, TripRequestDTO dto);

    // Delete a trip
    void deleteTrip(Long id);

    // Publish a trip
    TripResponseDTO publishTrip(Long id);

    // Complete a trip
    TripResponseDTO completeTrip(Long id);

    // Cancel a trip
    TripResponseDTO cancelTrip(Long id);
}
