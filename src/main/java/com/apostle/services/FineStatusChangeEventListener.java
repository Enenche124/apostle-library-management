package com.apostle.services;


import com.apostle.data.models.FineNotificationAudit;
import com.apostle.data.models.FineStatusChangeEvent;
import com.apostle.data.repositories.FineNotificationAuditRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class FineStatusChangeEventListener {
    private static final Logger log = LoggerFactory.getLogger(FineStatusChangeEventListener.class);
    private final EmailService emailService;
    private final FineNotificationAuditRepository auditRepository;  // For tracking notifications

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFineStatusChange(FineStatusChangeEvent event) {
        try {
            switch (event.getNewStatus()) {
                case PAID -> handlePaidStatus(event);
                case PENDING -> handlePendingStatus(event);
            }

            // Save notification audit
            saveNotificationAudit(event);

        } catch (Exception e) {
            log.error("Error processing fine status change event for fine: {}",
                    event.getFineId(), e);
        }
    }

    private void handlePaidStatus(FineStatusChangeEvent event) {
        log.info("Processing PAID status for fine: {}", event.getFineId());
        emailService.sendFinePaymentConfirmation(
                event.getBorrower(),
                event.getFineId()
        );
    }

    private void handlePendingStatus(FineStatusChangeEvent event) {
        log.info("Processing PENDING status for fine: {}", event.getFineId());
        emailService.sendFineCreatedNotification(
                event.getBorrower(),
                event.getFineId(),
                event.getRemainingAmount()
        );
    }

    private void saveNotificationAudit(FineStatusChangeEvent event) {
        FineNotificationAudit audit = FineNotificationAudit.builder()
                .fineId(event.getFineId())
                .oldStatus(event.getOldStatus())
                .newStatus(event.getNewStatus())
                .notificationTime(LocalDateTime.now())
                .recipientEmail(event.getBorrower())
                .build();

        auditRepository.save(audit);
    }
}