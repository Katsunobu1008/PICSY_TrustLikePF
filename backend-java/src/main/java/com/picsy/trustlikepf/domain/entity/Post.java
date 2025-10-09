// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/Post.java
package com.picsy.trustlikepf.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="posts")
public class Post {
    @Id
    @Column(name="post_id")
    private UUID postId;

    @Column(name="creator_id", nullable=false)
    private UUID creatorId;

    @Column(name="parent_post_id")
    private UUID parentPostId;

    @Column(name="original_post_id", nullable=false)
    private UUID originalPostId;

    @Column(name="royalty_rate", precision=5, scale=4)
    private BigDecimal royaltyRate;

    protected Post(){}

    public UUID getPostId(){ return postId; }
    public UUID getCreatorId(){ return creatorId; }
    public UUID getParentPostId(){ return parentPostId; }
    public UUID getOriginalPostId(){ return originalPostId; }
    public BigDecimal getRoyaltyRate(){ return royaltyRate; }
}
