
package com.apostle.services;

import com.apostle.data.models.*;
import com.apostle.data.repositories.BookRepository;
import com.apostle.data.repositories.BorrowBookRecordRepository;
import com.apostle.data.repositories.UserRepository;
import com.apostle.dtos.responses.BorrowResponse;
import com.apostle.exceptions.LibraryException;
import com.apostle.utils.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.apostle.utils.Mapper.mapToBorrowResponse;
import static com.apostle.utils.Mapper.mapToErrorResponse;

@Service
public class BorrowServiceImpl implements BorrowService {
    private final BorrowBookRecordRepository borrowBookRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineService fineService;

    private static final int LOAN_PERIOD_DAYS = 7;
    private static final double LOAN_FINE_AMOUNT_PER_DAY = 10.0;
    private static final double MAX_FINE_AMOUNT = 1000.0;

    @Autowired
    public BorrowServiceImpl(BorrowBookRecordRepository borrowBookRecordRepository,
                             BookRepository bookRepository,
                             UserRepository userRepository,
                             FineService fineService) {
        this.borrowBookRecordRepository = borrowBookRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.fineService = fineService;
    }

    private double getUserUnpaidFines(String userEmail) {
        List<BorrowBookRecord> borrowBookRecords = borrowBookRecordRepository.findByBorrowerAndStatus(userEmail, BorrowStatus.BORROWED);
        return borrowBookRecords.stream()
                .mapToDouble(BorrowBookRecord::getFineAmount)
                .sum();
    }

    @Override
    @Transactional
    public BorrowResponse borrowBook(String isbn, String userEmail) {
        try {
            double unpaidFines = getUserUnpaidFines(userEmail);
            if (unpaidFines >= MAX_FINE_AMOUNT) {
                throw new LibraryException("Cannot borrow book. Please pay your outstanding fines: $" + MAX_FINE_AMOUNT,
                        null, isbn, userEmail, unpaidFines);
            }

            Book book = bookRepository.findByIsbn(isbn)
                    .orElseThrow(() -> new IllegalArgumentException("Book with ISBN " + isbn + " not found"));

            if (!isBookAvailable(isbn)) {
                throw new IllegalArgumentException("Book is not available for borrowing");
            }

            User user = userRepository.findUserByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            BorrowBookRecord borrowBookRecord = createBorrowRecord(isbn, userEmail);
            BorrowBookRecord savedRecord = borrowBookRecordRepository.save(borrowBookRecord);

            return mapToBorrowResponse(savedRecord, "Book borrowed successfully", true);
        } catch (IllegalArgumentException e) {
            return new BorrowResponse(e.getMessage(), false, null, isbn, userEmail, null, null, null, null);
        }
    }

    @Override
    @Transactional
    public BorrowResponse returnBook(String borrowId) {
        try {
            BorrowBookRecord borrowBookRecord = borrowBookRecordRepository.findById(borrowId)
                    .orElseThrow(() -> new IllegalArgumentException("Borrow record not found"));

            if (borrowBookRecord.getStatus() != BorrowStatus.BORROWED) {
                throw new IllegalArgumentException("Book has already been returned or is not borrowed");
            }

            if (LocalDateTime.now().isAfter(borrowBookRecord.getDueDate())) {
                double finalFine = calculateFine(borrowId);
                if (finalFine > 0.0) {
                    Fine fine = fineService.createFine(borrowId, finalFine);
                    borrowBookRecord.setFineAmount(finalFine);
                    borrowBookRecord.setOverdue(true);
                    throw new LibraryException("Please pay the overdue fine before returning the book",
                            borrowId, borrowBookRecord.getBookIsbn(), borrowBookRecord.getBorrower(), finalFine);
                }
            }

            borrowBookRecord.setStatus(BorrowStatus.RETURNED);
            borrowBookRecord.setReturnDate(LocalDateTime.now());
            BorrowBookRecord savedRecord = borrowBookRecordRepository.save(borrowBookRecord);

            return mapToBorrowResponse(savedRecord, "Book returned successfully", true);
        } catch (LibraryException e) {
            return mapToErrorResponse(e.getMessage());
        }
    }

    @Override
    public List<BorrowBookRecord> getCurrentBorrowings(String userId) {
        return borrowBookRecordRepository.findByBorrowerAndStatus(userId, BorrowStatus.BORROWED);
    }

    @Override
    public List<BorrowBookRecord> getOverdueBorrowings() {
        return borrowBookRecordRepository.findByStatusAndDueDateBefore(BorrowStatus.BORROWED, LocalDateTime.now());
    }

    @Override
    public boolean isBookAvailable(String isbn) {
        return borrowBookRecordRepository.findByBookIsbnAndStatus(isbn, BorrowStatus.BORROWED).isEmpty();
    }

    @Override
    @Transactional
    public void updateOverdueStatus() {
        List<BorrowBookRecord> overdueBorrowings = getOverdueBorrowings();
        for (BorrowBookRecord record : overdueBorrowings) {
            if (!record.isOverdue()) {
                double fineAmount = calculateFine(record.getId());
                if (fineAmount > 0) {
                    record.setOverdue(true);
                    record.setFineAmount(fineAmount);
                    fineService.createFine(record.getId(), fineAmount);
                    borrowBookRecordRepository.save(record);
                }
            }
        }
    }

    @Override
    public double calculateFine(String borrowId) {
        BorrowBookRecord record = borrowBookRecordRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found"));

        if (record.getStatus() == BorrowStatus.RETURNED || !record.isOverdue()) {
            return 0.0;
        }

        long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), LocalDateTime.now());
        return Math.max(0, daysOverdue * LOAN_FINE_AMOUNT_PER_DAY);
    }


    @Override
    @Transactional
    public BorrowResponse payFine(String borrowId, double amount) {
        try {
            BorrowBookRecord borrowBookRecord = borrowBookRecordRepository.findById(borrowId)
                    .orElseThrow(() -> new LibraryException("Borrow record not found", borrowId, null, null, null));

            if (amount < borrowBookRecord.getFineAmount()) {
                throw new LibraryException(
                        "Payment amount is less than the fine amount",
                        borrowId,
                        borrowBookRecord.getBookIsbn(),
                        borrowBookRecord.getBorrower(),
                        borrowBookRecord.getFineAmount()
                );
            }

            // Get all fines for the user and find the one matching this borrow record
            List<Fine> userFines = fineService.getUserFines(borrowBookRecord.getBorrower());
            Fine fine = userFines.stream()
                    .filter(f -> f.getBorrowId().equals(borrowId))
                    .findFirst()
                    .orElseThrow(() -> new LibraryException(
                            "No fine record found for this borrow",
                            borrowId,
                            borrowBookRecord.getBookIsbn(),
                            borrowBookRecord.getBorrower(),
                            amount
                    ));

            // Process the payment
            Fine updatedFine = fineService.processPayment(fine.getId(), amount, PaymentMethod.CASH);

            // Update borrow record
            borrowBookRecord.setFineAmount(updatedFine.getRemainingAmount());
            if (updatedFine.getStatus() == FineStatus.PAID) {
                borrowBookRecord.setOverdue(false);
            }
            BorrowBookRecord savedRecord = borrowBookRecordRepository.save(borrowBookRecord);

            return Mapper.mapToBorrowResponse(savedRecord, "Fine paid successfully", true);
        } catch (LibraryException e) {
            return Mapper.mapToErrorResponse(e.getMessage());
        } catch (Exception e) {
            return Mapper.mapToErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }
    private BorrowBookRecord createBorrowRecord(String isbn, String userEmail) {
        BorrowBookRecord borrowBookRecord = new BorrowBookRecord();
        borrowBookRecord.setBorrower(userEmail);
        borrowBookRecord.setBookIsbn(isbn);
        borrowBookRecord.setBorrowDate(LocalDateTime.now());
        borrowBookRecord.setDueDate(LocalDateTime.now().plusDays(LOAN_PERIOD_DAYS));
        borrowBookRecord.setStatus(BorrowStatus.BORROWED);
        borrowBookRecord.setOverdue(false);
        borrowBookRecord.setFineAmount(0.0);
        return borrowBookRecord;
    }
}