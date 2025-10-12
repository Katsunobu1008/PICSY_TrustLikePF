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

    // ------------------------------------------------------------
    // like(): いいね取引 → E 行の更新（自己ループ減算＋配分）＋Txログ
    // 変更点:
    //  - ★ EvaluationRowService.Row を使用（ERowの置換）
    //  - ★ 自己いいね禁止を追加
    //  - ★ BigDecimal/Double のアンボクシングはローカル変数に落としてから
    //  - ★ rho_s を先に一回取得
    // ------------------------------------------------------------
    @Transactional
    public void like(LikeRequest req){
        // 冪等チェック
        if (txRepo.findByRequestId(req.requestId()).isPresent()) return;

        var post  = postRepo.findById(req.postId())
                .orElseThrow(() -> new IllegalArgumentException("post not found"));
        UUID actor = req.actorId();

        // ★ 自己いいね禁止（サーバ側で必ず弾く）
        if (post.getCreatorId().equals(actor)) {
            throw new IllegalStateException("SELF_LIKE_NOT_ALLOWED");
        }

        // 最終キュレーター c, 個人分配率 r_c（null 安全）
        var cUser = userRepo.findById(post.getCreatorId()).orElseThrow();
        BigDecimal rcBD = cUser.getCommissionRate();
        double r_c = (rcBD != null) ? rcBD.doubleValue() : 0.0;

        // ρ_s（原作の印税率）は1回のDB読み出しに集約（null 安全）
        UUID s = post.getOriginalPostId();
        BigDecimal rhoBD = postRepo.findById(s).orElseThrow().getRoyaltyRate();
        double rho_s = (rhoBD != null) ? rhoBD.doubleValue() : 0.0;

        // k_m（直前キュレーター）
        UUID k_m = null;
        if (post.getParentPostId() != null) {
            var parent = postRepo.findById(post.getParentPostId()).orElse(null);
            if (parent != null) k_m = parent.getCreatorId();
        }

        // 購買力検証: E_pp * c_p >= α
        double c_p = cRepo.findById(actor).orElseThrow().getValue();

        // ★ ERow → EvaluationRowService.Row に置換
        EvaluationRowService.Row row = eService.lockAndLoad(
                actor, Set.of(actor, cUser.getUserId(), s, (k_m != null ? k_m : actor))
        );
        double Epp = row.cols.get(actor).getValue();
        if (Epp * c_p < ALPHA) throw new IllegalStateException("INSUFFICIENT_PURCHASING_POWER");

        // 分配
        double Rc = r_c * ALPHA;
        double V  = (1 - r_c) * ALPHA;

        // 1) 自己ループ（予算）減算
        eService.add(row.cols.get(actor), -ALPHA);

        // 2) 最終キュレーター c へ
        eService.add(
                row.cols.computeIfAbsent(cUser.getUserId(), id -> new EvaluationMatrix(actor, id, 0.0)),
                Rc
        );

        // 3) 原作 s / 中間 k_m へ
        eService.add(
                row.cols.computeIfAbsent(s, id -> new EvaluationMatrix(actor, id, 0.0)),
                rho_s * V
        );
        if (k_m != null) {
            eService.add(
                    row.cols.computeIfAbsent(k_m, id -> new EvaluationMatrix(actor, id, 0.0)),
                    (1 - rho_s) * V
            );
        }

        // ログ（JSONはMVPのためStringで保持）
        String details = "{\"c\":\""+cUser.getUserId()+"\",\"r_c\":"+String.format("%.6f", r_c)
                +",\"k_m\":"+(k_m==null?"null":"\""+k_m+"\"")
                +",\"s\":\""+s+"\",\"rho_s\":"+String.format("%.6f", rho_s)+"}";

        var tx = new TransactionLog(
                "LIKE", actor, post.getPostId(),
                BigDecimal.valueOf(ALPHA), req.requestId(), details
        );
        txRepo.save(tx);
    }

    // ------------------------------------------------------------
    // quote(): 引用取引 → E 行の更新（自己ループ減算＋原作/編集配分）＋Txログ
    // 変更点:
    //  - ★ EvaluationRowService.Row を使用（ERowの置換）
    //  - ★ betaOverride のアンボクシング安全化
    //  - ★ ρ_s 取得は1回に集約
    //  - ★ 原作直引用か否かで配分先を分岐
    // ------------------------------------------------------------
    @Transactional
    public void quote(QuoteRequest req){
        if (txRepo.findByRequestId(req.requestId()).isPresent()) return;

        var quoted = postRepo.findById(req.postId()).orElseThrow();
        UUID actor = req.actorId();

        // ★ Double → double の安全な取り扱い
        Double betaOverride = req.betaOverride();
        double beta = (betaOverride != null) ? betaOverride.doubleValue() : defaultBeta;

        // 引用対象から s/k_prev を確定
        UUID s = quoted.getOriginalPostId();
        UUID k_prev = quoted.getCreatorId();
        boolean quotedIsOriginal = (quoted.getParentPostId() == null);

        // ρ_s は 1 回だけ取得（null 安全）
        BigDecimal rhoBD = postRepo.findById(s).orElseThrow().getRoyaltyRate();
        double rho_s = (rhoBD != null) ? rhoBD.doubleValue() : 0.0;

        // 購買力検証: E_kk * c_k >= β
        double c_k = cRepo.findById(actor).orElseThrow().getValue();
        // ★ ERow → EvaluationRowService.Row に置換
        EvaluationRowService.Row row = eService.lockAndLoad(actor, Set.of(actor, s, k_prev));
        double Ekk = row.cols.get(actor).getValue();
        if (Ekk * c_k < beta) throw new IllegalStateException("INSUFFICIENT_PURCHASING_POWER");

        // 1) 自己ループ（予算）減算
        eService.add(row.cols.get(actor), -beta);

        // 2) 配分：原作直引用 or 引用の引用
        if (quotedIsOriginal) {
            // 原作に全額
            eService.add(
                    row.cols.computeIfAbsent(s, id -> new EvaluationMatrix(actor, id, 0.0)),
                    beta
            );
        } else {
            // 原作印税 / 編集印税
            eService.add(
                    row.cols.computeIfAbsent(s, id -> new EvaluationMatrix(actor, id, 0.0)),
                    rho_s * beta
            );
            eService.add(
                    row.cols.computeIfAbsent(k_prev, id -> new EvaluationMatrix(actor, id, 0.0)),
                    (1 - rho_s) * beta
            );
        }

        String details = "{\"s\":\""+s+"\",\"rho_s\":"+String.format("%.6f", rho_s)
                +",\"k_prev\":\""+k_prev+"\"}";

        var tx = new TransactionLog(
                "QUOTE", actor, quoted.getPostId(),
                BigDecimal.valueOf(beta), req.requestId(), details
        );
        txRepo.save(tx);

        // ※ 将来: ここで「引用投稿を新規作成」まで同Txで実施する合体APIにしてもOK
    }
}
