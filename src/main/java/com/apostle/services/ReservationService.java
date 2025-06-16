package com.apostle.services;

import com.apostle.data.models.Reservation;

import java.util.List;

public interface ReservationService {
    Reservation reserveBook(String isbn, String userId);
    void cancelReservation(String reservationId);
    List<Reservation> getUserReservations(String userId);
    boolean isBookReserved(String isbn);
}
