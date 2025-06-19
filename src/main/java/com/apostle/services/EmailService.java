package com.apostle.services;

import com.apostle.data.models.FineNotificationAudit;
import com.apostle.data.models.NotificationType;
import com.apostle.data.repositories.FineNotificationAuditRepository;
import com.apostle.exceptions.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender emailSender;
    private final FineNotificationAuditRepository auditRepository;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 1000; // 1 second

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String PAYMENT_CONFIRMATION_TEMPLATE =
            """
                    Dear Library Member,
                    
                    Your payment for fine ID: %s has been confirmed. Thank you for your prompt payment!
                    
                    If you have any questions, please contact the library staff.
                    
                    Best regards,
                    Library Management""";

    private static final String FINE_CREATED_TEMPLATE =
            """
                    Dear Library Member,
                    
                    A new fine (ID: %s) of $%.2f has been created for your account.
                    Please settle this fine at your earliest convenience to maintain your library privileges.
                    
                    Best regards,
                    Library Management""";

    private static final String BOOK_DUE_REMINDER_TEMPLATE =
            """
                    Dear Library Member,
                    
                    This is a friendly reminder that the book '%s' is due on %s.
                    Please return it by the due date to avoid any late fees.
                    
                    Best regards,
                    Library Management""";

    private static final String OVERDUE_NOTIFICATION_TEMPLATE =
            """
                    Dear Library Member,
                    
                    The book '%s' is overdue. A fine of $%.2f has been applied to your account.
                    Please return the book as soon as possible to prevent additional charges.
                    
                    Best regards,
                    Library Management""";
    private static final String REGISTER_NOTIFICATION_TEMPLATE = """
            Dear %s,
            
            You have successfully registered for the library membership.
            
            Best regards,
            Library Management""";

    public EmailService(JavaMailSender emailSender,
                        FineNotificationAuditRepository auditRepository) {
        this.emailSender = emailSender;
        this.auditRepository = auditRepository;
    }

    public void sendRegistrationConfirmation(String toEmail, String firstName) {
        String subject = "Registration Confirmation";
        String message = String.format(REGISTER_NOTIFICATION_TEMPLATE, firstName);
        sendEmailWithRetry(toEmail, subject, message, firstName, NotificationType.REGISTER);
    }

    public void sendFinePaymentConfirmation(String toEmail, String fineId) {
        String subject = "Fine Payment Confirmation";
        String message = String.format(PAYMENT_CONFIRMATION_TEMPLATE, fineId);
        sendEmailWithRetry(toEmail, subject, message, fineId, NotificationType.PAYMENT_CONFIRMED);
    }

    public void sendFineCreatedNotification(String toEmail, String fineId, double amount) {
        String subject = "New Fine Notice";
        String message = String.format(FINE_CREATED_TEMPLATE, fineId, amount);
        sendEmailWithRetry(toEmail, subject, message, fineId, NotificationType.FINE_CREATED);
    }

    public void sendBookDueReminder(String toEmail, String bookTitle, LocalDate dueDate) {
        String subject = "Book Due Date Reminder";
        String message = String.format(BOOK_DUE_REMINDER_TEMPLATE,
                bookTitle, dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        sendEmailWithRetry(toEmail, subject, message, bookTitle, NotificationType.BOOK_DUE_SOON);
    }

    public void sendOverdueNotification(String toEmail, String bookTitle, double fineAmount) {
        String subject = "Book Overdue Notice";
        String message = String.format(OVERDUE_NOTIFICATION_TEMPLATE, bookTitle, fineAmount);
        sendEmailWithRetry(toEmail, subject, message, bookTitle, NotificationType.BOOK_OVERDUE);
    }

    public void sendBorrowConfirmation(String toEmail, String bookTitle, LocalDate dueDate) {
        String subject = "Book Borrow Confirmation";
        String message = String.format(
                """
                Dear Library Member,
                
                You have successfully borrowed '%s'. Please return by %s.
                
                Best regards,
                Library Management""",
                bookTitle, dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        sendEmailWithRetry(toEmail, subject, message, bookTitle, NotificationType.BORROW_CONFIRMED);
    }

    public void sendReturnConfirmation(String toEmail, String bookTitle) {
        String subject = "Book Return Confirmation";
        String message = String.format(
                """
                Dear Library Member,
                
                You have returned '%s'. Thank you!
                
                Best regards,
                Library Management""",
                bookTitle);
        sendEmailWithRetry(toEmail, subject, message, bookTitle, NotificationType.RETURN_CONFIRMED);
    }


    @Async
    protected void sendEmailWithRetry(String to, String subject, String text,
                                      String referenceId, NotificationType type) {

        if (to == null || to.isBlank() || !to.matches(".+@.+\\..+")) {
            log.error("Invalid email address: {}", to);
            auditFailedSend(to, referenceId, type, new IllegalArgumentException("Invalid email"));
            throw new NotificationException("Invalid email address: ");
        }
        if (subject == null || subject.isBlank() || text == null || text.isBlank()) {
            log.error("Invalid email content: subject={}, text={}", subject, text);
            auditFailedSend(to, referenceId, type, new IllegalArgumentException("Empty content"));
            throw new NotificationException("Subject or text cannot be empty");
        }
        Exception lastException = null;
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                emailSender.send(message);

                // Audit successful send
                auditSuccessfulSend(to, referenceId, type);
                log.info("Email sent successfully to: {}, subject: {}, type: {}",
                        to, subject, type);
                return; // Success, exit method
            } catch (MailException e) {
                lastException = e;
                log.warn("Attempt {} failed to send email to: {}, subject: {}",
                        attempt + 1, to, subject, e);
                if (attempt < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // If we get here, all attempts failed
        auditFailedSend(to, referenceId, type, lastException);
        log.error("All attempts to send email failed for: {}, subject: {}", to, subject);
        throw new NotificationException("Failed to send email after " + MAX_RETRIES + " attempts");
    }

    private void auditSuccessfulSend(String to, String referenceId, NotificationType type) {
        FineNotificationAudit audit = FineNotificationAudit.builder()
                .fineId(referenceId)
                .recipientEmail(to)
                .notificationTime(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .success(true)
                .notificationType(type)
                .build();
        auditRepository.save(audit);
    }

    private void auditFailedSend(String to, String referenceId, NotificationType type, Exception e) {
        FineNotificationAudit audit = FineNotificationAudit.builder()
                .fineId(referenceId)
                .recipientEmail(to)
                .notificationTime(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .success(false)
                .notificationType(type)
                .build();
        auditRepository.save(audit);
    }

}
