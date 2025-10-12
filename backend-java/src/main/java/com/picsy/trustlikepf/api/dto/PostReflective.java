// backend-java/src/main/java/com/picsy/trustlikepf/api/dto/PostReflective.java
package com.picsy.trustlikepf.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.picsy.trustlikepf.domain.entity.Post;

/**
 * API レイヤでの投稿ビュー。DB/ドメインの精度を保つため rate は BigDecimal を採用。
 * Record のフィールド順は JSON のプロパティ順にも影響する（Jackson既定）。
 */
public record PostReflective(
        UUID postId,
        UUID creatorId,
        UUID parentPostId,
        UUID originalPostId,
        String contentText,
        BigDecimal royaltyRate,
        Instant createdAt
) {
    public static PostReflective from(Post p){
        return new PostReflective(
                p.getPostId(),
                p.getCreatorId(),
                p.getParentPostId(),
                p.getOriginalPostId(),
                p.getContentText(),
                p.getRoyaltyRate(),   // 変換不要（BigDecimalのまま）
                p.getCreatedAt()      // Post は Instant を返す想定
        );
    }
}
