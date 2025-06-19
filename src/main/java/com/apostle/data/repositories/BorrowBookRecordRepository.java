package com.apostle.data.repositories;

import com.apostle.data.models.BorrowBookRecord;
import com.apostle.data.models.BorrowStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowBookRecordRepository extends MongoRepository<BorrowBookRecord,String> {
    List<BorrowBookRecord> findByBookIsbnAndStatus(String isbn, BorrowStatus status);

    List<BorrowBookRecord> findByBorrowerAndStatus(String userId, BorrowStatus status);

    List<BorrowBookRecord> findByStatusAndDueDateBefore(BorrowStatus borrowStatus, LocalDateTime now);

    List<BorrowBookRecord> findByDueDateBefore(@NotNull LocalDateTime dueDate);

    List<BorrowBookRecord> findByDueDate(@NotNull LocalDateTime dueDate);
    Optional<BorrowBookRecord> findReturnedBookByBookIsbnAndStatus(String isbn, BorrowStatus status);
}

