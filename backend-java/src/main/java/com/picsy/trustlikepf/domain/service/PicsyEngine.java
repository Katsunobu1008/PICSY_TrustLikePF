package com.picsy.trustlikepf.domain.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.domain.entity.ContributionVector;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;

@Service
public class PicsyEngine {
    private final EvaluationMatrixRepository eRepo;
    private final ContributionVectorRepository cRepo;
    private final UserRepository userRepo;

    public PicsyEngine(EvaluationMatrixRepository eRepo,
                       ContributionVectorRepository cRepo,
                       UserRepository userRepo) {
        this.eRepo = eRepo;
        this.cRepo = cRepo;
        this.userRepo = userRepo;
    }

@Scheduled(fixedDelayString = "${picsy.engine.interval.ms:300000}")
@Transactional
public void recalcC(){
    var activeUsers = userRepo.findByIsActiveTrue(); // ★アクティブのみ
    List<UUID> users = activeUsers.stream().map(com.picsy.trustlikepf.domain.entity.User::getUserId).toList();
    int N = users.size();
    if (N == 0) return;

    // 以降は同じ。初期cのロード
    Map<UUID, Double> c = new HashMap<>();
    cRepo.findAll().forEach(cv -> c.put(cv.getUserId(), cv.getValue()));
    users.forEach(u -> c.putIfAbsent(u, 1.0));

    // 行ごとのEをメモリへ
    Map<UUID, List<EvaluationMatrix>> rows = new HashMap<>();
    for (UUID u : users) {
        rows.put(u, eRepo.findByEvaluator(u));
    }

    for (int iter=0; iter<100; iter++){
        Map<UUID, Double> next = new HashMap<>();
        for (UUID j : users) next.put(j, 0.0);

        for (UUID i : users) {
            var row = rows.get(i);
            double c_i = c.get(i);
            double Eii = 0.0;
            double leak = 0.0; // ★ 凍結者（＝非アクティブ）や存在しない先に向かう量

            for (var em : row) {
                UUID j = em.getId().getEvaluateeId();
                double v = em.getValue();
                if (i.equals(j)) { Eii = v; continue; }

                // ★ 被評価者がアクティブ集合にいなければリークに加算（捨てない）
                if (!next.containsKey(j)) {
                    leak += v;
                } else {
                    next.put(j, next.get(j) + c_i * v);
                }
            }

            // ★ 仮想中央銀行法：対角 Eii を均等配分
            // ★ さらにリーク分も同様に均等配分（＝バーチャルトレジャリーの放出）
            double add = (N > 1) ? (Eii + leak) / (N - 1) : 0.0;
            if (add != 0.0){
                for (UUID j : users){
                    if (j.equals(i)) continue;
                    next.put(j, next.get(j) + c_i * add);
                }
            }
        }

        // 正規化（∑c = N）
        double sum = next.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum == 0) break;
        double scale = N / sum;
        double diff = 0.0;
        for (UUID u : users){
            double v = next.get(u) * scale;
            diff += Math.pow(v - c.get(u), 2);
            c.put(u, v);
        }
        if (Math.sqrt(diff) < 1e-9) break;
    }

    // 保存（丸め）
    for (UUID u : users){
        double v = Math.round(c.get(u) * 1_000_000d)/1_000_000d;
        cRepo.save(new ContributionVector(u, v));
    }
}
}
