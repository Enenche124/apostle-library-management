package com.apostle.data.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FineStatusChangeEvent {
    private final String fineId;
    private final FineStatus oldStatus;
    private final FineStatus newStatus;
    private final LocalDateTime timestamp;
    private final double remainingAmount;
    private final String borrower;
    private final double fineAmount;

}
