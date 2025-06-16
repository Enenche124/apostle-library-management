package com.apostle.data.repositories;

import com.apostle.data.models.Fine;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FineRepository extends MongoRepository<Fine, String> {
}
