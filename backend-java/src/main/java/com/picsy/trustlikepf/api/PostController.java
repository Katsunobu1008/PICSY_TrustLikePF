// backend-java/src/main/java/com/picsy/trustlikepf/api/PostController.java
package com.picsy.trustlikepf.api;

import com.picsy.trustlikepf.api.dto.CreatePostRequest;
import com.picsy.trustlikepf.domain.entity.Post;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
        // 原作 or 引用の判定
        UUID postId = UUID.randomUUID();
        UUID originalId = (req.parentPostId()==null) ? postId :
                          postRepo.findById(req.parentPostId()).orElseThrow().getOriginalPostId();

        // エンティティは簡易 setter を用意していないため、ここは JPA の insert 用に
        // エンティティ・コンストラクタ or Builder を追加しても良い。
        // MVP簡易：native insert でも可だが、ここでは簡便のためエンティティに setter を仮想的に置くイメージで記述します。

        Post p = new PostReflective(); // 下↓の補足参照
        ((PostReflective)p).setPostId(postId);
        ((PostReflective)p).setCreatorId(req.creatorId());
        ((PostReflective)p).setContentText(req.contentText());
        ((PostReflective)p).setParentPostId(req.parentPostId());
        ((PostReflective)p).setOriginalPostId(originalId);
        ((PostReflective)p).setRoyaltyRate(
                (req.parentPostId()==null) ?
                java.math.BigDecimal.valueOf(Optional.ofNullable(req.royaltyRate()).orElse(0.70)) : null);
        ((PostReflective)p).setCreatedAt(OffsetDateTime.now());

        postRepo.save(p);

        return Map.of("ok", true, "postId", postId, "originalPostId", originalId);
    }

    @GetMapping("/timeline")
    public List<Map<String,Object>> timeline(){
        // グローバル時系列（MVP）: created_at DESC
        // Repositoryにクエリを生やしても良いが、まずは findAll() → ソートでもOK
        List<Post> all = postRepo.findAll();
        all.sort(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
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
