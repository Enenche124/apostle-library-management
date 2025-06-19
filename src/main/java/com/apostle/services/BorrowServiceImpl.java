
package com.apostle.services;

import com.apostle.data.models.*;
import com.apostle.data.repositories.BookRepository;
import com.apostle.data.repositories.BorrowBookRecordRepository;
import com.apostle.data.repositories.UserRepository;
import com.apostle.dtos.responses.BorrowResponse;
import com.apostle.exceptions.LibraryException;
import com.apostle.utils.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.apostle.utils.Mapper.mapToBorrowResponse;

@Slf4j
@Service
public class BorrowServiceImpl implements BorrowService {
    private final BorrowBookRecordRepository borrowBookRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineService fineService;
    private final EmailService emailService;

    private static final int LOAN_PERIOD_DAYS = 7;
    private static final double LOAN_FINE_AMOUNT_PER_DAY = 10.0;
    private static final double MAX_FINE_AMOUNT = 1000.0;

    @Autowired
    public BorrowServiceImpl(BorrowBookRecordRepository borrowBookRecordRepository,
                             BookRepository bookRepository,
                             UserRepository userRepository,
                             FineService fineService, EmailService emailService) {
        this.borrowBookRecordRepository = borrowBookRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.fineService = fineService;
        this.emailService = emailService;
    }

    private double getUserUnpaidFines(String userEmail) {
        List<BorrowBookRecord> borrowBookRecords = borrowBookRecordRepository.findByBorrowerAndStatus(userEmail, BorrowStatus.BORROWED);
        return borrowBookRecords.stream()
                .mapToDouble(BorrowBookRecord::getFineAmount)
                .sum();
    }

    private void logBookOperationError(String bookIsbn, String userEmail, Exception e) {
        log.error("{} operation failed | ISBN: {} | User: {} | Error: {}",
                "Return",
                bookIsbn,
                userEmail,
                e.getMessage()
        );
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
            log.info("Borrowing book for user: email={}, id={}", userEmail, user.getId());

            BorrowBookRecord borrowBookRecord = createBorrowRecord(isbn, userEmail);
            BorrowBookRecord savedRecord = borrowBookRecordRepository.save(borrowBookRecord);
            log.info("Saved borrow record: borrower={}, isbn={}", savedRecord.getBorrower(), savedRecord.getBookIsbn());

            emailService.sendBorrowConfirmation(user.getEmail(), book.getTitle(), borrowBookRecord.getDueDate().toLocalDate());
            log.info("Book borrowed successfully: ISBN={}, user={}", isbn, userEmail);
            return mapToBorrowResponse(savedRecord, "Book borrowed successfully", true);
        } catch (IllegalArgumentException e) {
            return new BorrowResponse(e.getMessage(), false, null, isbn, userEmail, null, null, null, null);
        }
    }

    @Override
    @Transactional
    public BorrowResponse returnBook(String bookIsbn, String userEmail) {
        try {
            List<BorrowBookRecord> borrowBookRecords =  borrowBookRecordRepository.findByBookIsbnAndStatus(bookIsbn, BorrowStatus.BORROWED);
            if (borrowBookRecords.isEmpty()) {
                throw new IllegalArgumentException("Book is not currently borrowed");
            }
            BorrowBookRecord borrowBookRecord = borrowBookRecords.get(0);

//
//            if (!borrowBookRecord.getBorrower().equals(userEmail)) {
//                throw new IllegalArgumentException("Only the borrower can return this book");
//            }

            if (LocalDateTime.now().isAfter(borrowBookRecord.getDueDate())) {
                double finalFine = calculateFine(borrowBookRecord.getId());
                if (finalFine > 0.0) {
                    Fine fine = fineService.createFine(borrowBookRecord.getId(), finalFine);
                    borrowBookRecord.setFineAmount(finalFine);
                    borrowBookRecord.setOverdue(true);
                    borrowBookRecordRepository.save(borrowBookRecord);
                    throw new LibraryException("Please pay the overdue fine before returning the book",
                            borrowBookRecord.getId(), bookIsbn, userEmail, finalFine);
                }
            }
            Book book = bookRepository.findByIsbn(bookIsbn)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found"));



            borrowBookRecord.setStatus(BorrowStatus.RETURNED);
            borrowBookRecord.setReturnDate(LocalDateTime.now());
            BorrowBookRecord savedRecord = borrowBookRecordRepository.save(borrowBookRecord);

            emailService.sendReturnConfirmation(userEmail, book.getTitle());
            log.info("Book returned successfully: ISBN={}, user={}", bookIsbn, userEmail);

            return mapToBorrowResponse(savedRecord, "Book returned successfully", true);
        } catch (IllegalArgumentException e) {
//            log.error("Failed to return book: ISBN={}, user={}, error={}", bookIsbn, userEmail, e.getMessage());
            logBookOperationError(bookIsbn, userEmail, e);
            return new BorrowResponse(e.getMessage(), false, null, bookIsbn, userEmail, null, null, null, null);
        } catch (LibraryException e) {
//            log.error("Failed to return book: ISBN={}, user={}, error={}", bookIsbn, userEmail, e.getMessage());
            logBookOperationError(bookIsbn, userEmail, e);
            return new BorrowResponse(e.getMessage(), false, null, bookIsbn, userEmail, null, null, null, e.getFineAmount());
        }
    }
    @Override
    public List<BorrowBookRecord> getCurrentBorrowings(String userEmail) {
        log.info("Fetching borrowings for userEmail: {}", userEmail);
        List<BorrowBookRecord> records = borrowBookRecordRepository.findByBorrowerAndStatus(userEmail, BorrowStatus.BORROWED);
        log.info("Found borrowings: {}", records);
        return records;
    }

    @Override
    public List<BorrowBookRecord> getOverdueBorrowings() {
        return borrowBookRecordRepository.findByStatusAndDueDateBefore(BorrowStatus.BORROWED, LocalDateTime.now());
    }

    @Override
    public boolean isBookAvailable(String isbn) {
        if (bookRepository.findByIsbn(isbn).isEmpty()) {
            throw new IllegalArgumentException("Book with ISBN " + isbn + " not found");
        }

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