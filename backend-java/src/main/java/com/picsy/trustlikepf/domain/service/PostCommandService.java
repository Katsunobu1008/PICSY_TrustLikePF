// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/PostCommandService.java
package com.picsy.trustlikepf.domain.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.domain.entity.Post;
import com.picsy.trustlikepf.domain.repository.PostRepository;

@Service
public class PostCommandService {
    private final PostRepository postRepo;

    public PostCommandService(PostRepository postRepo) { this.postRepo = postRepo; }

    @Transactional
    public Post createOriginal(UUID creatorId, String content, BigDecimal royaltyRate){
        if (royaltyRate == null) {
            throw new IllegalArgumentException("royaltyRate required for original post");
        }
        var p = new Post();
        p.setPostId(UUID.randomUUID());
        p.setCreatorId(creatorId);
        p.setContentText(content);
        p.setOriginalPostId(p.getPostId());
        p.setRoyaltyRate(royaltyRate);
        return postRepo.save(p);
    }

    @Transactional
    public Post createQuote(UUID actorId, UUID targetPostId, String contentIfAny){
        var target = postRepo.findById(targetPostId).orElseThrow();
        var p = new Post();
        p.setPostId(UUID.randomUUID());
        p.setCreatorId(actorId);
        p.setContentText(contentIfAny == null ? "" : contentIfAny);
        p.setParentPostId(target.getPostId());
        p.setOriginalPostId(target.getOriginalPostId());
        p.setRoyaltyRate(null); // 引用はロイヤリティ設定なし
        return postRepo.save(p);
    }
}
