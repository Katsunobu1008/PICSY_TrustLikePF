package com.picsy.trustlikepf.domain.service;

import com.picsy.trustlikepf.domain.entity.Post;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PostCommandService {
    private final PostRepository postRepo;

    public PostCommandService(PostRepository postRepo) { this.postRepo = postRepo; }

    @Transactional
    public Post createOriginal(UUID creatorId, String content, BigDecimal royaltyRate){
        var p = new Post();
        // setter を用意していない場合は JPA に合わせてコンストラクタ等を追加してください
        // ここでは簡潔化: エンティティに setter を生やす運用を想定
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
        p.setContentText(contentIfAny);
        p.setParentPostId(target.getPostId());
        // 原作の継承
        p.setOriginalPostId(target.getOriginalPostId());
        // 版権率は編集では NULL （原作のみ設定）
        p.setRoyaltyRate(null);
        return postRepo.save(p);
    }
}
