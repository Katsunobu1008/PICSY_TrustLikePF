// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/ActionQueryService.java
package com.picsy.trustlikepf.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.picsy.trustlikepf.api.dto.PostActions;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.Post;
import com.picsy.trustlikepf.domain.entity.User;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;

@Service
public class ActionQueryService {

    private final PostRepository posts;
    private final UserRepository users;
    private final ContributionVectorRepository cvs;
    private final EvaluationMatrixRepository eRepo;

    private static final double ALPHA = 0.05;
    @Value("${picsy.beta.default:0.12}")
    private double defaultBeta;

    public ActionQueryService(PostRepository posts,
                              UserRepository users,
                              ContributionVectorRepository cvs,
                              EvaluationMatrixRepository eRepo) {
        this.posts = posts;
        this.users = users;
        this.cvs = cvs;
        this.eRepo = eRepo;
    }

    public Optional<PostActions> getActions(UUID postId, UUID actorId){
        Optional<Post> optPost = posts.findById(postId);
        if (optPost.isEmpty()) return Optional.empty();
        Post post = optPost.get();

        // 俳優/対象ユーザーの状態
        Optional<User> optActor  = users.findById(actorId);
        Optional<User> optTarget = users.findById(post.getCreatorId());

        if (optActor.isEmpty() || !optActor.get().isActive()) {
            return Optional.of(PostActions.disabledAll(ALPHA, defaultBeta));
        }
        boolean targetFrozen = optTarget.isEmpty() || !optTarget.get().isActive();

        // 購買力 Epp*c_p を求める
        double c_p = cvs.findById(actorId).map(cv -> cv.getValue()).orElse(1.0);
        double Epp = 1.0;
        List<EvaluationMatrix> row = eRepo.findByEvaluator(actorId);
        for (var em : row) {
            if (em.getId().getEvaluateeId().equals(actorId)) {
                Epp = em.getValue();
                break;
            }
        }
        double power = Epp * c_p;

        // like: 自己いいね禁止 / 対象凍結禁止 / 購買力判定
        boolean selfLikeForbidden = post.getCreatorId().equals(actorId); // ★ 自己リツイートのLIKEもここで禁止
        boolean likeEnabled = !selfLikeForbidden && !targetFrozen && power >= ALPHA;
        String likeReason = likeEnabled ? "OK" :
                selfLikeForbidden ? "SELF_LIKE_NOT_ALLOWED" :
                targetFrozen ? "TARGET_FROZEN" :
                "INSUFFICIENT_PURCHASING_POWER";

        // quote: 自己リツイートはOK / 対象凍結は禁止 / 購買力判定
        boolean quoteEnabled = !targetFrozen && power >= defaultBeta;
        String quoteReason = quoteEnabled ? "OK" :
                targetFrozen ? "TARGET_FROZEN" :
                "INSUFFICIENT_PURCHASING_POWER";

        return Optional.of(new PostActions(
                likeEnabled, likeReason,
                quoteEnabled, quoteReason,
                ALPHA, defaultBeta,
                power, power // 同一式 Epp*c_p
        ));
    }
}
