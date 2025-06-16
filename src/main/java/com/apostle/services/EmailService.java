package com.apostle.services;

import com.apostle.exceptions.NotificationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender emailSender;
    private final String FROM_EMAIL = "enenchejohn@gmail.com";  // Could be in application.properties

    public void sendFinePaymentConfirmation(String toEmail, String fineId) {
        String subject = "Fine Payment Confirmation";
        String message = String.format("Your payment for fine ID: %s has been confirmed. Thank you!", fineId);
        sendEmail(toEmail, subject, message);
    }

    public void sendFineCreatedNotification(String toEmail, String fineId, double amount) {
        String subject = "New Fine Notice";
        String message = String.format("A new fine (ID: %s) of $%.2f has been created for your account.",
                fineId, amount);
        sendEmail(toEmail, subject, message);
    }

    public void sendBookDueReminder(String toEmail, String bookTitle, LocalDate dueDate) {
        String subject = "Book Due Date Reminder";
        String message = String.format("Remember to return '%s' by %s to avoid fines.",
                bookTitle, dueDate.format(DateTimeFormatter.ISO_DATE));
        sendEmail(toEmail, subject, message);
    }

    public void sendOverdueNotification(String toEmail, String bookTitle, double fineAmount) {
        String subject = "Book Overdue Notice";
        String message = String.format("The book '%s' is overdue. A fine of $%.2f has been applied.",
                bookTitle, fineAmount);
        sendEmail(toEmail, subject, message);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);

            log.info("Email sent successfully to: {}, subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to: {}, subject: {}", to, subject, e);
            throw new NotificationException("Failed to send email notification", e);
        }
    }



}
