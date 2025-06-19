package com.apostle.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = {EmailConfig.class})
public class EmailConfigTest {
    @Autowired
    private JavaMailSender mailSender;

    @Test
   public void testEmailConfiguration() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("adahjohn419@gmail.com");
        message.setTo("enenchejohn56@gmail.com");  // Use your actual email for testing
        message.setSubject("Test Email");
        message.setText("This is a test email from the Fine Management System");

        assertDoesNotThrow(() -> {
            mailSender.send(message);
        }, "Email configuration is not working properly");
    }


}