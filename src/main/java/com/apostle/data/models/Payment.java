package com.apostle.data.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Payment {
    private String id;
    private double amount;
    private String fineId;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionReference;

}
