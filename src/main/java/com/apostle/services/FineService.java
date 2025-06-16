package com.apostle.services;

import com.apostle.data.models.Fine;
import com.apostle.data.models.Payment;
import com.apostle.data.models.PaymentMethod;

import java.util.List;

public interface FineService {
    Fine createFine(String borrowId, double amount);
    Fine getFineDetails(String fineId);
    List<Fine> getUserFines(String userEmail);
    Fine processPayment(String fineId, double amount, PaymentMethod paymentMethod); // Changed return type
    List<Payment> getFinePayments(String fineId);
    double calculateRemainingAmount(String fineId);
    void updateFineStatus(String fineId);
}