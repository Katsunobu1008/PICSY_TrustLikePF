// PostReflective.java
package com.picsy.trustlikepf.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.picsy.trustlikepf.domain.entity.Post;

public record PostReflective(
        UUID postId,
        UUID creatorId,
        String contentText,
        UUID parentPostId,
        UUID originalPostId,
        BigDecimal royaltyRate,
        OffsetDateTime createdAt
) {
    public static PostReflective from(Post p){
        return new PostReflective(
                p.getPostId(),
                p.getCreatorId(),
                p.getContentText(),
                p.getParentPostId(),
                p.getOriginalPostId(),
                p.getRoyaltyRate(),
                p.getCreatedAt()
        );
    }
}
