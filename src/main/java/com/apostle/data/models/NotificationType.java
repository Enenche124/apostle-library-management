package com.apostle.data.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    FINE_CREATED("New Fine Created"),
    PAYMENT_CONFIRMED("Payment Confirmed"),
    FINE_PAID("Fine Fully Paid"),
    BOOK_DUE_SOON("Book Due Soon"),
    BOOK_OVERDUE("Book Overdue"),
    REGISTER("Registered successfully"),
    BORROW_CONFIRMED("Book borrowed successfully"),
    RETURN_CONFIRMED("Return book confirmed");

    private final String description;

}
