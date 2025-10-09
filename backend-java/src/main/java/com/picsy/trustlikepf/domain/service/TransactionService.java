// TransactionService.java
package com.picsy.trustlikepf.domain.service;

import com.picsy.trustlikepf.api.dto.LikeRequest;
import com.picsy.trustlikepf.api.dto.QuoteRequest;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.TransactionLog;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import com.picsy.trustlikepf.domain.repository.TransactionLogRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

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
        // 冪等チェック
        if (txRepo.findByRequestId(req.requestId()).isPresent()) return;

        var post = postRepo.findById(req.postId())
                .orElseThrow(() -> new IllegalArgumentException("post not found"));
        UUID actor = req.actorId();

        // 最終キュレーターc, 個人分配率 r_c
        var cUser = userRepo.findById(post.getCreatorId()).orElseThrow();
        double r_c = cUser.getCommissionRate().doubleValue();

        // 親/原作/ρ_s 特定
        var parent = (post.getParentPostId() == null) ? null :
                postRepo.findById(post.getParentPostId()).orElse(null);
        UUID k_m = (parent == null) ? null : parent.getCreatorId();
        UUID s   = post.getOriginalPostId();
        double rho_s = postRepo.findById(s).orElseThrow().getRoyaltyRate().doubleValue();

        // 購買力検証: E_pp * c_p >= α
        double c_p = cRepo.findById(actor).orElseThrow().getValue();
        ERow row = eService.lockAndLoad(actor, Set.of(actor, cUser.getUserId(), s, (k_m!=null?k_m:actor)));
        double Epp = row.cols.get(actor).getValue();
        if (Epp * c_p < ALPHA) throw new IllegalStateException("insufficient purchasing power.");

        // 分配
        double Rc = r_c * ALPHA;
        double V  = (1 - r_c) * ALPHA;

        // 1) 自己ループ減算
        eService.add(row.cols.get(actor), -ALPHA);
        // 2) 最終キュレーター
        eService.add(row.cols.computeIfAbsent(cUser.getUserId(), id -> new EvaluationMatrix(actor,id,0.0)), Rc);
        // 3) 原作/編集
        eService.add(row.cols.computeIfAbsent(s, id -> new EvaluationMatrix(actor,id,0.0)), rho_s * V);
        if (k_m != null) {
            eService.add(row.cols.computeIfAbsent(k_m, id -> new EvaluationMatrix(actor,id,0.0)), (1 - rho_s) * V);
        }

        // ログ（amount は BigDecimal、details は JSON文字列）
        String details = "{\"c\":\""+cUser.getUserId()+"\",\"r_c\":"+String.format("%.6f", r_c)
                +",\"k_m\":"+(k_m==null?"null":"\""+k_m+"\"")
                +",\"s\":\""+s+"\",\"rho_s\":"+String.format("%.6f", rho_s)+"}";
        var tx = new TransactionLog("LIKE", actor, post.getPostId(),
                BigDecimal.valueOf(ALPHA), req.requestId(), details);
        txRepo.save(tx);
    }

    @Transactional
    public void quote(QuoteRequest req){
        if (txRepo.findByRequestId(req.requestId()).isPresent()) return;

        var post = postRepo.findById(req.postId()).orElseThrow();
        UUID actor = req.actorId();
        double beta = (req.betaOverride()!=null)? req.betaOverride() : defaultBeta;

        var parentPost = postRepo.findById(post.getParentPostId()).orElseThrow();
        UUID s = parentPost.getOriginalPostId();
        double rho_s = postRepo.findById(s).orElseThrow().getRoyaltyRate().doubleValue();
        UUID k_prev = parentPost.getCreatorId();

        double c_k = cRepo.findById(actor).orElseThrow().getValue();
        ERow row = eService.lockAndLoad(actor, Set.of(actor, s, k_prev));
        double Ekk = row.cols.get(actor).getValue();
        if (Ekk * c_k < beta) throw new IllegalStateException("insufficient purchasing power.");

        // 1) 自己ループ減算
        eService.add(row.cols.get(actor), -beta);
        // 2) 分配
        if (post.getParentPostId().equals(s)) {
            eService.add(row.cols.get(s), beta);
        } else {
            eService.add(row.cols.get(s), rho_s * beta);
            eService.add(row.cols.get(k_prev), (1 - rho_s) * beta);
        }

        String details = "{\"s\":\""+s+"\",\"rho_s\":"+String.format("%.6f", rho_s)
                +",\"k_prev\":\""+k_prev+"\"}";
        var tx = new TransactionLog("QUOTE", actor, post.getPostId(),
                BigDecimal.valueOf(beta), req.requestId(), details);
        txRepo.save(tx);
    }
}
