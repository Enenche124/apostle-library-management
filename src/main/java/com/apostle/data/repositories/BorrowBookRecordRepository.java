package com.apostle.data.repositories;

import com.apostle.data.models.BorrowBookRecord;
import com.apostle.data.models.BorrowStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowBookRecordRepository extends MongoRepository<BorrowBookRecord,String> {
    List<BorrowBookRecord> findByBookIsbnAndStatus(String isbn, BorrowStatus status);

    List<BorrowBookRecord> findByBorrowerAndStatus(String userId, BorrowStatus status);

    List<BorrowBookRecord> findByStatusAndDueDateBefore(BorrowStatus borrowStatus, LocalDateTime now);

    List<BorrowBookRecord> findByDueDateBefore(@NotNull LocalDateTime dueDate);

    List<BorrowBookRecord> findByDueDateAfter(@NotNull LocalDateTime dueDate);
}

