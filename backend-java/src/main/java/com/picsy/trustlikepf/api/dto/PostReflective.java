// PostReflective.java
package com.picsy.trustlikepf.api.dto;

import com.picsy.trustlikepf.domain.entity.Post;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PostReflective(
        UUID postId,
        UUID creatorId,
        String contentText,
        UUID parentPostId,
        UUID originalPostId,
        BigDecimal royaltyRate,
        Instant createdAt
) {
    public static PostReflective from(Post p, String contentTextIfAny){
        return new PostReflective(
                p.getPostId(), p.getCreatorId(),
                contentTextIfAny, // 今は簡易。将来は posts テーブル拡張/別テーブルでメディアや本文を解決
                p.getParentPostId(), p.getOriginalPostId(),
                p.getRoyaltyRate(),
                p.getCreatedAt()
        );
    }
}
