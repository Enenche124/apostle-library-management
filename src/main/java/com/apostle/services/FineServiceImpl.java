
package com.apostle.services;

import com.apostle.data.models.*;
import com.apostle.data.repositories.BorrowBookRecordRepository;
import com.apostle.data.repositories.FineRepository;
import com.apostle.data.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FineServiceImpl implements FineService {
    private final FineRepository fineRepository;
    private final BorrowBookRecordRepository borrowRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public FineServiceImpl(FineRepository fineRepository,
                           BorrowBookRecordRepository borrowRepository,
                           PaymentRepository paymentRepository) {
        this.fineRepository = fineRepository;
        this.borrowRepository = borrowRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public Fine createFine(String borrowId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Fine amount must be greater than zero");
        }

        BorrowBookRecord borrowRecord = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found"));

        List<Fine> existingFines = fineRepository.findAll().stream()
                .filter(fine -> fine.getBorrowId().equals(borrowId))
                .toList();

        if (!existingFines.isEmpty()) {
            throw new IllegalStateException("Fine already exists for this borrow record");
        }

        Fine fine = new Fine();
        fine.setBookIsbn(borrowRecord.getBookIsbn());
        fine.setBorrower(borrowRecord.getBorrower());
        fine.setFineAmount(amount);
        fine.setStatus(FineStatus.PENDING);
        fine.setBorrowId(borrowId);
        fine.setCreatedDate(LocalDateTime.now());
        fine.setLastUpdatedDate(LocalDateTime.now());
        fine.setPayments(new ArrayList<>());
        fine.setRemainingAmount(amount);

        return fineRepository.save(fine);
    }

    @Override
    public Fine getFineDetails(String fineId) {
        return fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found with ID: " + fineId));
    }

    @Override
    public List<Fine> getUserFines(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        return fineRepository.findAll().stream()
                .filter(fine -> fine.getBorrower().equals(userEmail))
                .toList();
    }


    @Override
    @Transactional
    public Fine processPayment(String fineId, double amount, PaymentMethod paymentMethod) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        Fine fine = getFineDetails(fineId);

        if (fine.getStatus() == FineStatus.PAID) {
            throw new IllegalStateException("Fine has already been paid");
        }

        if (amount > fine.getRemainingAmount()) {
            throw new IllegalArgumentException(
                    String.format("Payment amount %.2f exceeds remaining fine amount %.2f",
                            amount, fine.getRemainingAmount()));
        }

        Payment payment = createPayment(fineId, amount, paymentMethod);
        Payment savedPayment = paymentRepository.save(payment);

        updateFineAfterPayment(fine, savedPayment);

        return getFineDetails(fineId); // Return the updated fine
    }

    @Override
    public List<Payment> getFinePayments(String fineId) {
        Fine fine = getFineDetails(fineId);
        return fine.getPayments() != null ? fine.getPayments() : new ArrayList<>();
    }

    @Override
    public double calculateRemainingAmount(String fineId) {
        Fine fine = getFineDetails(fineId);
        return fine.getRemainingAmount();
    }

    @Override
    @Transactional
    public void updateFineStatus(String fineId) {
        Fine fine = getFineDetails(fineId);
        FineStatus oldStatus = fine.getStatus();

        if (fine.getRemainingAmount() == 0 && fine.getStatus() != FineStatus.PAID) {
            fine.setStatus(FineStatus.PAID);
        } else if (fine.getRemainingAmount() > 0 && fine.getStatus() != FineStatus.PENDING) {
            fine.setStatus(FineStatus.PENDING);
        }

        if (oldStatus != fine.getStatus()) {
            fine.setLastUpdatedDate(LocalDateTime.now());
            fineRepository.save(fine);
        }
    }

    private Payment createPayment(String fineId, double amount, PaymentMethod paymentMethod) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setAmount(amount);
        payment.setFineId(fineId);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionReference(generateTransactionReference());
        return payment;
    }

    private void updateFineAfterPayment(Fine fine, Payment payment) {
        if (fine.getPayments() == null) {
            fine.setPayments(new ArrayList<>());
        }

        fine.getPayments().add(payment);
        fine.setRemainingAmount(fine.getRemainingAmount() - payment.getAmount());
        fine.setLastUpdatedDate(LocalDateTime.now());

        if (fine.getRemainingAmount() == 0) {
            fine.setStatus(FineStatus.PAID);
        }

        fineRepository.save(fine);
    }

    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}