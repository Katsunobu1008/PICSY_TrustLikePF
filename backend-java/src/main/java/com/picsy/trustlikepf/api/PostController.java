// backend-java/src/main/java/com/picsy/trustlikepf/api/PostController.java
package com.picsy.trustlikepf.api;

import com.picsy.trustlikepf.api.dto.CreatePostRequest;
import com.picsy.trustlikepf.domain.entity.Post;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostRepository postRepo;

    public PostController(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    @PostMapping
    @Transactional
    public Map<String,Object> create(@RequestBody CreatePostRequest req){
        UUID postId = UUID.randomUUID();

        UUID originalId;
        if (req.parentPostId() == null) {
            originalId = postId; // 原作
        } else {
            var parent = postRepo.findById(req.parentPostId())
                    .orElseThrow(() -> new IllegalArgumentException("parent post not found"));
            originalId = parent.getOriginalPostId();
        }

        Post p = new Post();
        p.setPostId(postId);
        p.setCreatorId(req.creatorId());
        p.setContentText(Objects.requireNonNullElse(req.contentText(), "")); // 空文字許容
        p.setParentPostId(req.parentPostId());
        p.setOriginalPostId(originalId);
        if (req.parentPostId() == null) {
            // 原作のみ royalty_rate 必須（DDL 合致）
            double rho = Objects.requireNonNullElse(req.royaltyRate(), 0.70);
            p.setRoyaltyRate(BigDecimal.valueOf(rho));
        } else {
            p.setRoyaltyRate(null);
        }
        p.setCreatedAt(OffsetDateTime.now());

        postRepo.save(p);

        return Map.of("ok", true, "postId", postId, "originalPostId", originalId);
    }

    @GetMapping("/timeline")
    public List<Map<String,Object>> timeline(){
        List<Post> all = postRepo.findAll();
        all.sort(
            Comparator.comparing(
                Post::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed()
        );
        List<Map<String,Object>> out = new ArrayList<>();
        for (Post p : all){
            out.add(Map.of(
                "postId", p.getPostId(),
                "creatorId", p.getCreatorId(),
                "contentText", p.getContentText(),
                "parentPostId", p.getParentPostId(),
                "originalPostId", p.getOriginalPostId(),
                "royaltyRate", p.getRoyaltyRate(),
                "createdAt", p.getCreatedAt()
            ));
        }
        return out;
    }
}
