package com.apostle.data.repositories;

import com.apostle.data.models.FineNotificationAudit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FineNotificationAuditRepository extends MongoRepository<FineNotificationAudit, String> {
    List<FineNotificationAudit> findByFineId(String fineId);
}
