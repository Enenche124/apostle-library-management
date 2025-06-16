package com.apostle.data.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "borrowed_books")
public class BorrowBookRecord {
    @Id
    private String id;
    @NotBlank(message = "Book ISBN is required")
    private String bookIsbn;
    @NotBlank(message = "Borrower is required")
    private String borrower;
    @NotNull
    private LocalDateTime borrowDate;
    @NotNull
    private LocalDateTime dueDate;
    private BorrowStatus status;
    private LocalDateTime returnDate;

//    private LocalDateTime dueSoonDate;
    private boolean isOverdue;
    private double fineAmount;
}
