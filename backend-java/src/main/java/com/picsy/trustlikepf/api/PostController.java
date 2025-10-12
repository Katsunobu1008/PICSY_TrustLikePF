// backend-java/src/main/java/com/picsy/trustlikepf/api/PostController.java
package com.picsy.trustlikepf.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.picsy.trustlikepf.api.dto.AffordanceResponse;
import com.picsy.trustlikepf.api.dto.CreatePostRequest;
import com.picsy.trustlikepf.api.dto.LikeRequest;
import com.picsy.trustlikepf.api.dto.PostReflective;
import com.picsy.trustlikepf.api.dto.QuoteRequest;
import com.picsy.trustlikepf.domain.entity.Post;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import com.picsy.trustlikepf.domain.service.AffordanceService;
import com.picsy.trustlikepf.domain.service.PostCommandService;
import com.picsy.trustlikepf.domain.service.TransactionService;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostCommandService postCmd;
    private final TransactionService tx;
    private final PostRepository posts;
    private final AffordanceService affordance; // ★ 購買力可否の判定

    public PostController(PostCommandService postCmd,
                          TransactionService tx,
                          PostRepository posts,
                          AffordanceService affordance) {
        this.postCmd = postCmd;
        this.tx = tx;
        this.posts = posts;
        this.affordance = affordance;
    }

    /** 原作の作成 */
    @PostMapping
    public ResponseEntity<PostReflective> createOriginal(@RequestBody CreatePostRequest req){
        BigDecimal rr = (req.royaltyRate() == null) ? null : BigDecimal.valueOf(req.royaltyRate());
        Post p = postCmd.createOriginal(req.creatorId(), req.contentText(), rr);
        return ResponseEntity.ok(PostReflective.from(p));
    }

    /** 引用の作成（投稿データ自体を作る） */
    @PostMapping("/{postId}/quote")
    public ResponseEntity<PostReflective> createQuote(@PathVariable UUID postId,
                                                      @RequestBody QuoteRequest req){
        Post p = postCmd.createQuote(req.actorId(), postId, /* contentIfAny */ null);
        return ResponseEntity.ok(PostReflective.from(p));
    }

    /** いいね（取引を切る） */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> like(@PathVariable UUID postId, @RequestBody LikeRequest req){
        if (!postId.equals(req.postId())) return ResponseEntity.badRequest().build();
        tx.like(req);
        return ResponseEntity.accepted().build();
    }

    /** 引用（取引を切る） */
    @PostMapping("/{postId}/quote/tx")
    public ResponseEntity<Void> quoteTx(@PathVariable UUID postId, @RequestBody QuoteRequest req){
        if (!postId.equals(req.postId())) return ResponseEntity.badRequest().build();
        tx.quote(req);
        return ResponseEntity.accepted().build();
    }

    /** フィード（新しい順） */
    @GetMapping("/feed")
    public ResponseEntity<List<PostReflective>> feed(@RequestParam(defaultValue = "50") int size){
        var page = PageRequest.of(0, Math.min(Math.max(size,1), 200),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PostReflective> out = posts.findAll(page).stream().map(PostReflective::from).toList();
        return ResponseEntity.ok(out);
    }

    /** アクション可否（UIのボタン活性と理由表示に使用） */
    @GetMapping("/{postId}/actions")
    public ResponseEntity<AffordanceResponse> actions(@PathVariable UUID postId,
                                                      @RequestParam("actor") UUID actor){
        return ResponseEntity.ok(affordance.forActorAndPost(actor, postId));
    }
}
