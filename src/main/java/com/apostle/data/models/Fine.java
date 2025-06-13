package com.apostle.data.models;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

}
