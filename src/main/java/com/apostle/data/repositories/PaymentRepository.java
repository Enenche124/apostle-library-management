package com.apostle.data.repositories;

import com.apostle.data.models.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository  extends MongoRepository<Payment, String> {
}
