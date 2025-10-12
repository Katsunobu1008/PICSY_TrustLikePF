// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/Post.java
package com.picsy.trustlikepf.domain.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name="posts")
public class Post {
    @Id
    @Column(name="post_id")
    private UUID postId;

    @Column(name="creator_id", nullable=false)
    private UUID creatorId;

    @Column(name="content_text", nullable=false)
    private String contentText;

    @Column(name="parent_post_id")
    private UUID parentPostId;

    @Column(name="original_post_id", nullable=false)
    private UUID originalPostId;

    @Column(name="royalty_rate", precision=5, scale=4)
    private BigDecimal royaltyRate;

    // ★ created_at は OffsetDateTime に統一
    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    // ★ JPAのための no-arg。サービス層から new するので public にする（MVP）
    public Post(){}

    // ---------- getters ----------
    public UUID getPostId(){ return postId; }
    public UUID getCreatorId(){ return creatorId; }
    public String getContentText(){ return contentText; }
    public UUID getParentPostId(){ return parentPostId; }
    public UUID getOriginalPostId(){ return originalPostId; }
    public BigDecimal getRoyaltyRate(){ return royaltyRate; }
    public OffsetDateTime getCreatedAt(){ return createdAt; }

    // ---------- setters (MVP用) ----------
    public void setPostId(UUID postId){ this.postId = postId; }
    public void setCreatorId(UUID creatorId){ this.creatorId = creatorId; }
    public void setContentText(String contentText){ this.contentText = contentText; }
    public void setParentPostId(UUID parentPostId){ this.parentPostId = parentPostId; }
    public void setOriginalPostId(UUID originalPostId){ this.originalPostId = originalPostId; }
    public void setRoyaltyRate(BigDecimal royaltyRate){ this.royaltyRate = royaltyRate; }
    public void setCreatedAt(OffsetDateTime createdAt){ this.createdAt = createdAt; }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
