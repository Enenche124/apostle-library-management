package com.apostle.data.models;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "fines")
@Data
public class Fine {
    @Id
    private String id;
    @NotBlank
    private String bookIsbn;
    @NotBlank
    private String borrower;
    @NotNull
    private double fineAmount;
    private FineStatus status;

    private String borrowId;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
    private List<Payment> payments; // Track payment history
    private double remainingAmount;


}
