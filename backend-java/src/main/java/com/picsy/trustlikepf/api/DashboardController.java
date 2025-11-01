// backend-java/src/main/java/com/picsy/trustlikepf/api/DashboardController.java
package com.picsy.trustlikepf.api;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.picsy.trustlikepf.api.dto.ActiveUsersResponse;
import com.picsy.trustlikepf.api.dto.DashboardEvaluationResponse;
import com.picsy.trustlikepf.domain.entity.ContributionVector;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.User;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;
import com.picsy.trustlikepf.domain.service.DashboardMetricsService;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final ContributionVectorRepository cRepo;
    private final EvaluationMatrixRepository eRepo;
    private final UserRepository userRepo;
    private final DashboardMetricsService metricsService;

    public DashboardController(ContributionVectorRepository cRepo,
                               EvaluationMatrixRepository eRepo,
                               UserRepository userRepo,
                               DashboardMetricsService metricsService) {
        this.cRepo = cRepo;
        this.eRepo = eRepo;
        this.userRepo = userRepo;
        this.metricsService = metricsService;
    }

    // 既存: 単ユーザーの核心指標（MapのままでOK）
    @GetMapping("/core-metrics/{userId}")
    public Map<String,Object> core(@PathVariable UUID userId){
        double c = cRepo.findById(userId).map(ContributionVector::getValue).orElse(1.0);
        double eii = eRepo.findByEvaluator(userId).stream()
                .filter(em -> em.getId().getEvaluateeId().equals(userId))
                .mapToDouble(EvaluationMatrix::getValue).findFirst().orElse(1.0);
        double purchasing = eii * c;
        return Map.of("ok", true, "c", c, "Eii", eii, "purchasingPower", purchasing);
    }

    // ✅ 追加: アクティブユーザー一覧（DTOで返す）
    @GetMapping("/active-users")
    public ActiveUsersResponse activeUsers(){
        List<User> active = userRepo.findByIsActiveTrue();
        var users = active.stream()
                .map(u -> new ActiveUsersResponse.UserItem(u.getUserId(), u.getName()))
                .toList();
        return new ActiveUsersResponse(true, users);
    }

    // ✅ 追加: 評価行列（アクティブ×アクティブ）（DTOで返す）
    @GetMapping("/eval-matrix")
    public com.picsy.trustlikepf.api.dto.EvalMatrixResponse evalMatrixActiveOnly(){
        List<User> active = userRepo.findByIsActiveTrue();
        Set<UUID> activeIds = active.stream().map(User::getUserId).collect(Collectors.toSet());

        var rows = new ArrayList<com.picsy.trustlikepf.api.dto.EvalMatrixResponse.Row>();
        for (User i : active) {
            for (var em : eRepo.findByEvaluator(i.getUserId())) {
                UUID j = em.getId().getEvaluateeId();
                if (!activeIds.contains(j)) continue; // アクティブのみ
                rows.add(new com.picsy.trustlikepf.api.dto.EvalMatrixResponse.Row(i.getUserId(), j, em.getValue()));
            }
        }
        return new com.picsy.trustlikepf.api.dto.EvalMatrixResponse(true, rows);
    }

    @GetMapping("/evaluation-summary")
    public DashboardEvaluationResponse evaluationSummary() {
        return metricsService.summarize();
    }
}
