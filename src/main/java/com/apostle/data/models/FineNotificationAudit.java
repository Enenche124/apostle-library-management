package com.apostle.data.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "fine_notification_audits")
@Data
@Builder
public class FineNotificationAudit {
    @Id
    private String id;
    private String fineId;
    private FineStatus oldStatus;
    private FineStatus newStatus;
    private LocalDateTime notificationTime;
    private String recipientEmail;
}
