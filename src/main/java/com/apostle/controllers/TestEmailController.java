package com.apostle.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class TestEmailController {
    private final JavaMailSender javaMailSender;


    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("enenchejohn56@gmail.com");
        message.setSubject("Test Email");
        message.setText("This is a test email");
      try {
          javaMailSender.send(message);
          return ResponseEntity.ok("Email sent successfully");
      }catch (MatchException e){
          return ResponseEntity.badRequest().body(e.getMessage());
      }
    }
}
