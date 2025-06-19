package com.apostle.services;

import com.apostle.data.models.BorrowBookRecord;
import com.apostle.data.models.FineStatusChangeEvent;
import com.apostle.data.repositories.BorrowBookRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryNotificationService {
    private final EmailService emailService;
    private final BorrowBookRecordRepository borrowRepository;

    @EventListener
    @Transactional(readOnly = true)
    public void handleFineStatusChange(FineStatusChangeEvent event) {
        switch (event.getNewStatus()) {
            case PAID -> emailService.sendFinePaymentConfirmation(
                    event.getBorrower(),
                    event.getFineId()
            );
            case PENDING -> emailService.sendFineCreatedNotification(
                    event.getBorrower(),
                    event.getFineId(),
                    event.getFineAmount()
            );
        }
    }

    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional(readOnly = true)
    public void checkAndNotifyDueDates() {
        LocalDate today = LocalDate.now();
        List<BorrowBookRecord> dueSoonBooks = borrowRepository.findByDueDate(today.plusDays(2).atStartOfDay());

        for (BorrowBookRecord record : dueSoonBooks) {
            emailService.sendBookDueReminder(
                    record.getBorrower(),
                    record.getBookIsbn(),
                    LocalDate.from(record.getDueDate())

            );
        }
    }

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional(readOnly = true)
    public void checkAndNotifyOverdue() {
        LocalDate today = LocalDate.now();
        List<BorrowBookRecord> overdueBooks = borrowRepository.findByDueDateBefore(today.atStartOfDay());

        for (BorrowBookRecord record : overdueBooks) {
            emailService.sendOverdueNotification(
                    record.getBorrower(),
                    record.getBookIsbn(),
                    record.getFineAmount()
            );
        }
    }


}
