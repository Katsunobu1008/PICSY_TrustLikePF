// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/TransactionService.java
package com.picsy.trustlikepf.domain.service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

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
        if (txRepo.findByRequestId(req.requestId()).isPresent()) return;

        var post  = postRepo.findById(req.postId()).orElseThrow(() -> new IllegalArgumentException("post not found"));
        UUID actor = req.actorId();

        // 自己いいね禁止
        if (post.getCreatorId().equals(actor)) {
            throw new IllegalStateException("SELF_LIKE_NOT_ALLOWED");
        }
        // 凍結チェック（支払者・受益者）
        var actorUser  = userRepo.findById(actor).orElseThrow();
        if (!actorUser.isActive()) throw new IllegalStateException("ACCOUNT_FROZEN");
        var cUser = userRepo.findById(post.getCreatorId()).orElseThrow();
        if (!cUser.isActive()) throw new IllegalStateException("TARGET_FROZEN");

        double r_c  = (cUser.getCommissionRate() == null) ? 0.0 : cUser.getCommissionRate().doubleValue();
        UUID s      = post.getOriginalPostId();
        double rho_s= postRepo.findById(s).orElseThrow().getRoyaltyRate().doubleValue();

        UUID k_m = null;
        if (post.getParentPostId() != null) {
            var parent = postRepo.findById(post.getParentPostId()).orElse(null);
            if (parent != null) k_m = parent.getCreatorId();
        }

        double c_p = cRepo.findById(actor).orElseThrow().getValue();

        var row = eService.lockAndLoad(actor, Set.of(actor, cUser.getUserId(), s, (k_m != null ? k_m : actor)));
        double Epp = row.cols().get(actor).getValue();
        if (Epp * c_p < ALPHA) throw new IllegalStateException("INSUFFICIENT_PURCHASING_POWER");

        double Rc = r_c * ALPHA;
        double V  = (1 - r_c) * ALPHA;

        eService.add(row.cols().get(actor), -ALPHA);
        eService.add(row.cols().computeIfAbsent(cUser.getUserId(), id -> new EvaluationMatrix(actor, id, 0.0)), Rc);
        eService.add(row.cols().computeIfAbsent(s,      id -> new EvaluationMatrix(actor, id, 0.0)), rho_s * V);
        if (k_m != null) {
            eService.add(row.cols().computeIfAbsent(k_m, id -> new EvaluationMatrix(actor, id, 0.0)), (1 - rho_s) * V);
        }

        String details = "{\"c\":\""+cUser.getUserId()+"\",\"r_c\":"+String.format("%.6f", r_c)
                +",\"k_m\":"+(k_m==null?"null":"\""+k_m+"\"")
                +",\"s\":\""+s+"\",\"rho_s\":"+String.format("%.6f", rho_s)+"}";
        var tx = new TransactionLog("LIKE", actor, post.getPostId(), BigDecimal.valueOf(ALPHA), req.requestId(), details);
        txRepo.save(tx);
    }

    @Transactional
    public void quote(QuoteRequest req){
        if (txRepo.findByRequestId(req.requestId()).isPresent()) return;

        var quoted = postRepo.findById(req.postId()).orElseThrow();
        UUID actor = req.actorId();

        // 凍結チェック（支払者・受益者）
        var actorUser = userRepo.findById(actor).orElseThrow();
        if (!actorUser.isActive()) throw new IllegalStateException("ACCOUNT_FROZEN");
        var targetUser = userRepo.findById(quoted.getCreatorId()).orElseThrow();
        if (!targetUser.isActive()) throw new IllegalStateException("TARGET_FROZEN");

        double beta = (req.betaOverride() != null) ? req.betaOverride().doubleValue() : defaultBeta;

        UUID s = quoted.getOriginalPostId();
        UUID k_prev = quoted.getCreatorId();
        boolean quotedIsOriginal = (quoted.getParentPostId() == null);

        double rho_s = postRepo.findById(s).orElseThrow().getRoyaltyRate().doubleValue();

        double c_k = cRepo.findById(actor).orElseThrow().getValue();
        var row = eService.lockAndLoad(actor, Set.of(actor, s, k_prev));
        double Ekk = row.cols().get(actor).getValue();
        if (Ekk * c_k < beta) throw new IllegalStateException("INSUFFICIENT_PURCHASING_POWER");

        eService.add(row.cols().get(actor), -beta);
        if (quotedIsOriginal) {
            eService.add(row.cols().computeIfAbsent(s, id -> new EvaluationMatrix(actor, id, 0.0)), beta);
        } else {
            eService.add(row.cols().computeIfAbsent(s,      id -> new EvaluationMatrix(actor, id, 0.0)), rho_s * beta);
            eService.add(row.cols().computeIfAbsent(k_prev, id -> new EvaluationMatrix(actor, id, 0.0)), (1 - rho_s) * beta);
        }

        String details = "{\"s\":\""+s+"\",\"rho_s\":"+String.format("%.6f", rho_s)+",\"k_prev\":\""+k_prev+"\"}";
        var tx = new TransactionLog("QUOTE", actor, quoted.getPostId(), BigDecimal.valueOf(beta), req.requestId(), details);
        txRepo.save(tx);
    }
}
