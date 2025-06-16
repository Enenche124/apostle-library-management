package com.apostle.dtos.responses;

import com.apostle.data.models.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BorrowResponse {
    private String message;
    private boolean success;
    private String borrowId;
    private String bookIsbn;
    private String borrower;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private BorrowStatus status;
    private Double fineAmount;

}
