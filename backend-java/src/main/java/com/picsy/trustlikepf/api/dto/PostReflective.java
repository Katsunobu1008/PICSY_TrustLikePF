// backend-java/src/main/java/com/picsy/trustlikepf/api/dto/PostReflective.java
package com.picsy.trustlikepf.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.picsy.trustlikepf.domain.entity.Post;

public record PostReflective(
        UUID postId,
        UUID creatorId,
        String contentText,
        UUID parentPostId,
        UUID originalPostId,
        BigDecimal royaltyRate,
        Instant createdAt
        // 将来: actions, powerHints などを追加しやすい
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
