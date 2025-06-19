package com.apostle.data.repositories;

import com.apostle.data.models.FineNotificationAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineNotificationAuditRepository extends MongoRepository<FineNotificationAudit, String> {
    List<FineNotificationAudit> findByFineId(String fineId);
}
