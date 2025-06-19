package com.apostle.dtos.requests;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PayFineRequest {
    @Min(value = 0, message = "Amount must be greater than zero")
    private double amount;
}