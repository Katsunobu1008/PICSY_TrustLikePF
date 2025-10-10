// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/Post.java
package com.picsy.trustlikepf.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="posts")
public class Post {
    @Id
    @Column(name="post_id")
    private UUID postId;

    @Column(name="creator_id", nullable=false)
    private UUID creatorId;

    @Column(name="content_text", nullable=false)        // ★ 追加
    private String contentText;

    @Column(name="parent_post_id")
    private UUID parentPostId;

    @Column(name="original_post_id", nullable=false)
    private UUID originalPostId;

    @Column(name="royalty_rate", precision=5, scale=4)
    private BigDecimal royaltyRate;

    @Column(name="created_at")                          // 任意で付与（表示用）
    private java.time.OffsetDateTime createdAt;

    protected Post(){}

    public UUID getPostId(){ return postId; }
    public UUID getCreatorId(){ return creatorId; }
    public String getContentText(){ return contentText; }   // ★ getter
    public UUID getParentPostId(){ return parentPostId; }
    public UUID getOriginalPostId(){ return originalPostId; }
    public BigDecimal getRoyaltyRate(){ return royaltyRate; }

// Post.java の末尾に setter を暫定追加（MVP簡便のため）
public void setPostId(UUID id){ this.postId = id; }
public void setCreatorId(UUID id){ this.creatorId = id; }
public void setContentText(String t){ this.contentText = t; }
public void setParentPostId(UUID id){ this.parentPostId = id; }
public void setOriginalPostId(UUID id){ this.originalPostId = id; }
public void setRoyaltyRate(BigDecimal r){ this.royaltyRate = r; }
public void setCreatedAt(java.time.OffsetDateTime dt){ this.createdAt = dt; }

}
