package com.apostle.services;

import com.apostle.data.models.BorrowBookRecord;
import com.apostle.data.models.Fine;
import com.apostle.data.models.Reservation;

import java.util.List;

public interface UserHistoryService {
    List<BorrowBookRecord> getUserBorrowHistory(String userId);
    List<Reservation> getUserReservationHistory(String userId);
    List<Fine> getUserFineHistory(String userId);

}
