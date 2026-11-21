package com.covoiturage.service.reservation;


import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.repository.PointArretRepository;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.repository.TripRepository;
import com.covoiturage.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final PointArretRepository pointArretRepository;
    private final ReservationMapper reservationMapper;

}