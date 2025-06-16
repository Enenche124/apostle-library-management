package com.apostle.data.repositories;

import com.apostle.data.models.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository  extends MongoRepository<Payment, String> {
}
