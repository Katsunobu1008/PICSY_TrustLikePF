// backend-java/src/main/java/com/picsy/trustlikepf/api/PostController.java
package com.picsy.trustlikepf.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;      // ★ 追加

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;  // ★ 追加
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.picsy.trustlikepf.api.dto.CreatePostRequest;
import com.picsy.trustlikepf.api.dto.LikeRequest;                                  // ★ 追加
import com.picsy.trustlikepf.api.dto.PostActions;
import com.picsy.trustlikepf.api.dto.PostReflective;
import com.picsy.trustlikepf.api.dto.QuoteRequest;
import com.picsy.trustlikepf.domain.entity.Post;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;
import com.picsy.trustlikepf.domain.service.ActionQueryService;
import com.picsy.trustlikepf.domain.service.EvaluationRowService;
import com.picsy.trustlikepf.domain.service.PostCommandService;
import com.picsy.trustlikepf.domain.service.TransactionService;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostCommandService postCmd;
    private final TransactionService  tx;
    private final PostRepository      posts;
    private final ActionQueryService  actions;               // ★ 追加
        // ★追加
    private final EvaluationRowService eService;
    private final ContributionVectorRepository cRepo;
    private final UserRepository userRepo;

    public PostController(PostCommandService postCmd, TransactionService tx, PostRepository posts,
                          ActionQueryService actions) {
        this.postCmd = postCmd;
        this.tx = tx;
        this.posts = posts;
        this.actions = actions;                               // ★ 追加
    }

    /** 原作の作成 */
    @PostMapping
    public ResponseEntity<PostReflective> createOriginal(@RequestBody CreatePostRequest req){
        // royaltyRate は Double → BigDecimal に明示変換（コンパイルエラー回避 & 精度担保）
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
        if (!postId.equals(req.postId())) {
            return ResponseEntity.badRequest().build();
        }
        tx.like(req);
        return ResponseEntity.accepted().build();
    }

    /** 引用（取引を切る） */
    @PostMapping("/{postId}/quote/tx")
    public ResponseEntity<Void> quoteTx(@PathVariable UUID postId, @RequestBody QuoteRequest req){
        if (!postId.equals(req.postId())) {
            return ResponseEntity.badRequest().build();
        }
        tx.quote(req);
        return ResponseEntity.accepted().build();
    }

    /** フィード（新しい順） */
    @GetMapping("/feed")
    public ResponseEntity<List<PostReflective>> feed(@RequestParam(defaultValue = "50") int size){
        var page = PageRequest.of(0, Math.min(Math.max(size,1), 200),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PostReflective> out = posts.findAll(page).stream()
                .map(PostReflective::from)
                .toList();
        return ResponseEntity.ok(out);
    }

 /** アクション可否（UIのボタン活性と理由表示に使用） */
    @GetMapping("/{postId}/actions")
    public ResponseEntity<PostActionsResponse> actions(@PathVariable UUID postId, @RequestParam("actor") UUID actor){
        var post = posts.findById(postId).orElseThrow();
        var actorUser = userRepo.findById(actor).orElseThrow();
        var targetUser = userRepo.findById(post.getCreatorId()).orElseThrow();

        // まず購買力
        double c = cRepo.findById(actor).orElseThrow().getValue();
        var row = eService.lockAndLoad(actor, java.util.Set.of(actor));
        double Epp = row.cols().get(actor).getValue(); // Row#cols() は下で説明

        boolean canLike = true; String likeReason = "OK";
        if (!actorUser.isActive()) { canLike = false; likeReason = "ACCOUNT_FROZEN"; }
        else if (!targetUser.isActive()) { canLike = false; likeReason = "TARGET_FROZEN"; }
        else if (post.getCreatorId().equals(actor)) { canLike = false; likeReason = "SELF_LIKE_NOT_ALLOWED"; }
        else if (Epp * c < 0.05 /* α */) { canLike = false; likeReason = "INSUFFICIENT_PURCHASING_POWER"; }

        // 引用は actor が凍結でなければ基本OK（実際の quote() 内で最終チェック）
        boolean canQuote = actorUser.isActive();
        String quoteReason = canQuote ? "OK" : "ACCOUNT_FROZEN";

        var out = new PostActionsResponse(
                canLike, likeReason,
                canQuote, quoteReason,
                Epp, c, Epp*c,
                0.05,           // α
                0.12            // β default（必要なら設定値を注入）
        );
        return ResponseEntity.ok(out);
    }
}
