// backend-java/src/main/java/com/picsy/trustlikepf/api/DashboardController.java
package com.picsy.trustlikepf.api;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.picsy.trustlikepf.domain.entity.ContributionVector;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.User;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final ContributionVectorRepository cRepo;
    private final EvaluationMatrixRepository eRepo;
    private final UserRepository userRepo;

    public DashboardController(ContributionVectorRepository cRepo,
                               EvaluationMatrixRepository eRepo,
                               UserRepository userRepo) {
        this.cRepo = cRepo; this.eRepo = eRepo; this.userRepo = userRepo;
    }

    // 既存: 単ユーザーの核心指標
    @GetMapping("/core-metrics/{userId}")
    public Map<String,Object> core(@PathVariable UUID userId){
        double c = cRepo.findById(userId).map(ContributionVector::getValue).orElse(1.0);
        double eii = eRepo.findByEvaluator(userId).stream()
                .filter(em -> em.getId().getEvaluateeId().equals(userId))
                .mapToDouble(EvaluationMatrix::getValue).findFirst().orElse(1.0);
        double purchasing = eii * c;
        return Map.of("ok", true, "c", c, "Eii", eii, "purchasingPower", purchasing);
    }

    // ✅ 追加: アクティブユーザー一覧（ダッシュボード/ヒートマップ用）
    @GetMapping("/active-users")
    public Map<String, Object> activeUsers(){
        List<User> active = userRepo.findByIsActiveTrue();
        List<Map<String,Object>> users = active.stream()
                .map(u -> Map.of("userId", u.getUserId(), "name", u.getName()))
                .collect(Collectors.toList());
        return Map.of("ok", true, "users", users);
    }

    // ✅ 追加: 評価行列（アクティブ×アクティブ）の（i,j,value）行データ
    @GetMapping("/eval-matrix")
    public Map<String, Object> evalMatrixActiveOnly(){
        // i(評価者)の候補
        List<User> active = userRepo.findByIsActiveTrue();
        Set<UUID> activeIds = active.stream().map(User::getUserId).collect(Collectors.toSet());

        // i ごとに行を取り出し、j(被評価者)もアクティブだけ残す
        List<Map<String,Object>> rows = new ArrayList<>();
        for (User i : active) {
            List<EvaluationMatrix> line = eRepo.findByEvaluator(i.getUserId());
            for (var em : line) {
                UUID j = em.getId().getEvaluateeId();
                if (!activeIds.contains(j)) continue; // アクティブのみ
                rows.add(Map.of(
                        "evaluatorId", i.getUserId(),
                        "evaluateeId", j,
                        "value", em.getValue()
                ));
            }
        }
        return Map.of("ok", true, "rows", rows);
    }
}
