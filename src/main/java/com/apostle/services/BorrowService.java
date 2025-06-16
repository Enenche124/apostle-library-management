package com.apostle.services;

import com.apostle.data.models.BorrowBookRecord;
import com.apostle.dtos.responses.BorrowResponse;

import java.util.List;

public interface BorrowService {
    BorrowResponse borrowBook(String isbn, String userEmail);
    BorrowResponse returnBook(String borrowId);

    List<BorrowBookRecord> getCurrentBorrowings(String userId);
    List<BorrowBookRecord> getOverdueBorrowings();
    boolean isBookAvailable(String isbn);
    BorrowResponse payFine(String borrowId, double amount);

    void updateOverdueStatus();
    double calculateFine(String borrowId);

}
