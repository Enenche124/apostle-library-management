package com.apostle.data.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reservations")
@Data
public class Reservation {
    @Id
    private String id;
    @NotBlank
    private String bookIsbn;
    @NotBlank
    private String borrower;
    @NotNull
    private LocalDateTime reservationDate;
    private ReservationStatus status;
}
