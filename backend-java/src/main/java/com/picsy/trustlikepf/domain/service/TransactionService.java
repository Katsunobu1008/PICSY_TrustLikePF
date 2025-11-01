// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/TransactionService.java
package com.picsy.trustlikepf.domain.service;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.api.dto.LikeRequest;
import com.picsy.trustlikepf.api.dto.QuoteRequest;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.TransactionLog;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import com.picsy.trustlikepf.domain.repository.TransactionLogRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final EvaluationRowService eService;
    private final ContributionVectorRepository cRepo;
    private final PostRepository postRepo;
    private final TransactionLogRepository txRepo;
    private final UserRepository userRepo;

    public TransactionService(EvaluationRowService eService,
                              ContributionVectorRepository cRepo,
                              PostRepository postRepo,
                              TransactionLogRepository txRepo,
                              UserRepository userRepo) {
        this.eService = eService;
        this.cRepo = cRepo;
        this.postRepo = postRepo;
        this.txRepo = txRepo;
        this.userRepo = userRepo;
    }

    private static final double ALPHA = 0.05; // いいねのα

    @Value("${picsy.beta.default:0.12}")
    private double defaultBeta;

    @Transactional
    public void like(LikeRequest req){
        UUID actor = requireUuid(req.actorId(), "ACTOR_ID_REQUIRED");
        UUID postId = requireUuid(req.postId(), "POST_ID_REQUIRED");
        UUID requestId = requireUuid(req.requestId(), "REQUEST_ID_REQUIRED");

        if (txRepo.findByRequestId(requestId).isPresent()) {
            log.debug("Duplicate like request {} ignored", requestId);
            return;
        }

        var post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("POST_NOT_FOUND"));

        // 自己いいね禁止
        if (post.getCreatorId().equals(actor)) {
            throw new IllegalStateException("SELF_LIKE_NOT_ALLOWED");
        }
        // 凍結チェック（支払者・受益者）
        var actorUser = userRepo.findById(actor)
                .orElseThrow(() -> new IllegalArgumentException("ACTOR_NOT_FOUND"));
        if (!actorUser.isActive()) throw new IllegalStateException("ACCOUNT_FROZEN");
        var cUser = userRepo.findById(post.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("POST_CREATOR_NOT_FOUND"));
        if (!cUser.isActive()) throw new IllegalStateException("TARGET_FROZEN");

        double r_c  = (cUser.getCommissionRate() == null) ? 0.0 : cUser.getCommissionRate().doubleValue();
        UUID originalPostId = post.getOriginalPostId();
        var originalPost = postRepo.findById(originalPostId)
                .orElseThrow(() -> new IllegalArgumentException("ORIGINAL_POST_NOT_FOUND"));
        UUID originalCreatorId = originalPost.getCreatorId();
        double rho_s = originalPost.getRoyaltyRate().doubleValue();

        UUID k_m = null;
        if (post.getParentPostId() != null) {
            var parent = postRepo.findById(post.getParentPostId()).orElse(null);
            if (parent != null) k_m = parent.getCreatorId();
        }

        double c_p = cRepo.findById(actor).orElseThrow().getValue();

        var ensureCols = new LinkedHashSet<UUID>();
        ensureCols.add(actor);
        ensureCols.add(cUser.getUserId());
        ensureCols.add(originalCreatorId);
        if (k_m != null) ensureCols.add(k_m);

        var row = eService.lockAndLoad(actor, ensureCols);
        double Epp = row.cols().get(actor).getValue();
        if (Epp * c_p < ALPHA) throw new IllegalStateException("INSUFFICIENT_PURCHASING_POWER");

        double Rc = r_c * ALPHA;
        double V  = (1 - r_c) * ALPHA;

        eService.add(row.cols().get(actor), -ALPHA);
        eService.add(row.cols().computeIfAbsent(cUser.getUserId(), id -> new EvaluationMatrix(actor, id, 0.0)), Rc);

        double originalShare = rho_s * V;
        double parentShare = 0.0;
        if (k_m != null) {
            parentShare = (1 - rho_s) * V;
        } else {
            originalShare += (1 - rho_s) * V;
        }

        eService.add(row.cols().computeIfAbsent(originalCreatorId, id -> new EvaluationMatrix(actor, id, 0.0)), originalShare);
        if (parentShare > 0.0 && k_m != null) {
            eService.add(row.cols().computeIfAbsent(k_m, id -> new EvaluationMatrix(actor, id, 0.0)), parentShare);
        }

        String details = "{\"c\":\""+cUser.getUserId()+"\",\"r_c\":"+format6(r_c)
                +",\"k_m\":"+(k_m==null?"null":"\""+k_m+"\"")
                +",\"s\":\""+originalPostId+"\",\"rho_s\":"+format6(rho_s)+"}";
        var tx = new TransactionLog("LIKE", actor, post.getPostId(), BigDecimal.valueOf(ALPHA), requestId, details);
        txRepo.save(tx);
    }

    @Transactional
    public void quote(QuoteRequest req){
        UUID actor = requireUuid(req.actorId(), "ACTOR_ID_REQUIRED");
        UUID targetPostId = requireUuid(req.postId(), "POST_ID_REQUIRED");
        UUID requestId = requireUuid(req.requestId(), "REQUEST_ID_REQUIRED");
        double beta = resolveBeta(req);

        if (txRepo.findByRequestId(requestId).isPresent()) {
            log.debug("Duplicate quote request {} ignored", requestId);
            return;
        }

        var quoted = postRepo.findById(targetPostId)
                .orElseThrow(() -> new IllegalArgumentException("POST_NOT_FOUND"));

        // 凍結チェック（支払者・受益者）
        var actorUser = userRepo.findById(actor)
                .orElseThrow(() -> new IllegalArgumentException("ACTOR_NOT_FOUND"));
        if (!actorUser.isActive()) throw new IllegalStateException("ACCOUNT_FROZEN");
        var targetUser = userRepo.findById(quoted.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("POST_CREATOR_NOT_FOUND"));
        if (!targetUser.isActive()) throw new IllegalStateException("TARGET_FROZEN");

        UUID originalPostId = quoted.getOriginalPostId();
        if (originalPostId == null) throw new IllegalStateException("ORIGINAL_POST_ID_MISSING");
        var originalPost = postRepo.findById(originalPostId)
                .orElseThrow(() -> new IllegalArgumentException("ORIGINAL_POST_NOT_FOUND"));
        UUID originalCreatorId = originalPost.getCreatorId();
        UUID k_prev = quoted.getCreatorId();
        boolean quotedIsOriginal = (quoted.getParentPostId() == null);

        var royalty = originalPost.getRoyaltyRate();
        if (royalty == null) throw new IllegalStateException("ORIGINAL_ROYALTY_NOT_SET");
        double rho_s = royalty.doubleValue();
        if (Double.isNaN(rho_s) || Double.isInfinite(rho_s) || rho_s < 0.0 || rho_s > 1.0) {
            throw new IllegalStateException("INVALID_ROYALTY_RATE");
        }

        double c_k = cRepo.findById(actor)
                .orElseThrow(() -> new IllegalArgumentException("CONTRIBUTION_VECTOR_MISSING")).getValue();
        var ensureCols = new LinkedHashSet<UUID>();
        ensureCols.add(actor);
        ensureCols.add(originalCreatorId);
        ensureCols.add(k_prev);

        var row = eService.lockAndLoad(actor, ensureCols);
        double Ekk = row.cols().get(actor).getValue();
        if (Ekk * c_k < beta) throw new IllegalStateException("INSUFFICIENT_PURCHASING_POWER");

        eService.add(row.cols().get(actor), -beta);
        double originalShare;
        double parentShare;
        if (quotedIsOriginal) {
            originalShare = beta;
            parentShare = 0.0;
            eService.add(row.cols().computeIfAbsent(originalCreatorId, id -> new EvaluationMatrix(actor, id, 0.0)), beta);
        } else {
            originalShare = rho_s * beta;
            parentShare = (1 - rho_s) * beta;
            eService.add(row.cols().computeIfAbsent(originalCreatorId, id -> new EvaluationMatrix(actor, id, 0.0)), originalShare);
            eService.add(row.cols().computeIfAbsent(k_prev, id -> new EvaluationMatrix(actor, id, 0.0)), parentShare);
        }

        String details = parentShare > 0.0
                ? String.format("{\"original\":\"%s\",\"quoted_post\":\"%s\",\"beta\":%s,\"rho_s\":%s,\"original_share\":%s,\"parent_share\":{\"user\":\"%s\",\"amount\":%s}}",
                originalPostId, quoted.getPostId(), format6(beta), format6(rho_s), format6(originalShare), k_prev, format6(parentShare))
                : String.format("{\"original\":\"%s\",\"quoted_post\":\"%s\",\"beta\":%s,\"rho_s\":%s,\"original_share\":%s,\"parent_share\":null}",
                originalPostId, quoted.getPostId(), format6(beta), format6(rho_s), format6(originalShare));

        if (log.isDebugEnabled()) {
            log.debug("Quote tx actor={} post={} beta={} originalShare={} parentShare={}",
                    actor, targetPostId, format6(beta), format6(originalShare), parentShare > 0.0 ? format6(parentShare) : "0.000000");
        }

        var tx = new TransactionLog("QUOTE", actor, quoted.getPostId(), BigDecimal.valueOf(beta), requestId, details);
        txRepo.save(tx);
    }

    private static UUID requireUuid(UUID value, String code) {
        if (value == null) throw new IllegalArgumentException(code);
        return value;
    }

    private double resolveBeta(QuoteRequest req) {
        double fallback = defaultBeta;
        if (Double.isNaN(fallback) || Double.isInfinite(fallback) || fallback <= 0.0) {
            throw new IllegalStateException("INVALID_DEFAULT_BETA");
        }
        Double override = req.betaOverride();
        if (override == null) {
            return fallback;
        }
        double beta = override.doubleValue();
        if (Double.isNaN(beta) || Double.isInfinite(beta) || beta <= 0.0) {
            throw new IllegalArgumentException("INVALID_BETA_OVERRIDE");
        }
        return beta;
    }

    private static String format6(double value) {
        return String.format("%.6f", value);
    }
}
