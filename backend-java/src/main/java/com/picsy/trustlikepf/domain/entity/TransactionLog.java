// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/TransactionLog.java
package com.picsy.trustlikepf.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "transaction_log")
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    // LIKE / QUOTE（MVP は String でOK）
    @Column(name = "transaction_type", nullable = false, length = 16)
    private String transactionType;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "target_post_id", nullable = false)
    private UUID targetPostId;

    // NUMERIC(12,6) は BigDecimal が安全
    @Column(name = "amount", nullable = false, precision = 12, scale = 6)
    private BigDecimal amount;

    @Column(name = "request_id", nullable = false, unique = true)
    private UUID requestId;

    // JSONB: MVP は String として保存（後で Hibernate Types に置換可）
    @Column(name = "details", columnDefinition = "jsonb")
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransactionLog() {}

    public TransactionLog(String transactionType,
                          UUID actorId,
                          UUID targetPostId,
                          BigDecimal amount,
                          UUID requestId,
                          String details) {
        this.transactionType = transactionType;
        this.actorId = actorId;
        this.targetPostId = targetPostId;
        this.amount = amount;
        this.requestId = requestId;
        this.details = details;
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    // ----- getter / setter -----
    public Long getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public UUID getActorId() { return actorId; }
    public UUID getTargetPostId() { return targetPostId; }
    public BigDecimal getAmount() { return amount; }
    public UUID getRequestId() { return requestId; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }

    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }
    public void setTargetPostId(UUID targetPostId) { this.targetPostId = targetPostId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }
    public void setDetails(String details) { this.details = details; }
}
