// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/EvaluationRowService.java
package com.picsy.trustlikepf.domain.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;

@Service
public class EvaluationRowService {
    // ---- 公開ネスト型：公開APIに載せても警告にならない ----
    public static final class Row {
        public final UUID evaluator;
        public final Map<UUID, EvaluationMatrix> cols = new HashMap<>();
        public Row(UUID evaluator){ this.evaluator = evaluator; }
    }

    private final EvaluationMatrixRepository repo;

    public EvaluationRowService(EvaluationMatrixRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Row lockAndLoad(UUID evaluatorId, Collection<UUID> ensureCols) {
        List<EvaluationMatrix> list = repo.lockRowByEvaluator(evaluatorId); // 行ロック
        var row = new Row(evaluatorId);
        list.forEach(e -> row.cols.put(e.getId().getEvaluateeId(), e));
        for (UUID colId : ensureCols) {
            row.cols.computeIfAbsent(colId, cid -> repo.save(
                    new EvaluationMatrix(evaluatorId, cid, 0.0)
            ));
        }
        return row;
    }

    public void add(EvaluationMatrix em, double delta){
        double v = em.getValue() + delta;
        if (v < 0) v = 0;
        em.setValue(round6(v));
    }

    static double round6(double x){ return Math.round(x * 1_000_000d)/1_000_000d; }

    // 末尾にメソッド追加
@Transactional
public void applyRecovery(UUID evaluatorId, double gamma) {
    // 行ロックして行全体をフェッチ
    var list = repo.lockRowByEvaluator(evaluatorId);

    // 行を Map に張る
    Map<UUID, EvaluationMatrix> row = new HashMap<>();
    for (var em : list) row.put(em.getId().getEvaluateeId(), em);

    // 自己ループと他列に分解
    EvaluationMatrix self = row.computeIfAbsent(evaluatorId,
            id -> repo.save(new EvaluationMatrix(evaluatorId, id, 0.0)));

    // 1) 非対角: (1-γ) 倍、負方向の丸め誤差は0に吸収
    for (var entry : row.entrySet()){
        var j = entry.getKey();
        var em = entry.getValue();
        if (!j.equals(evaluatorId)) {
            double v = em.getValue() * (1.0 - gamma);
            em.setValue(round6(Math.max(0.0, v)));
        }
    }
    // 2) 対角: Ebb += γ (1 - Ebb)
    double eii = self.getValue();
    self.setValue(round6(eii + gamma * (1.0 - eii)));

    // 3) 行和=1の微調整（最終的に自己ループへ寄せる）
    double sum = 0.0;
    for (var em : row.values()) sum += em.getValue();
    double diff = round6(1.0 - sum);
    if (Math.abs(diff) > 1e-9) {
        self.setValue(round6(Math.max(0.0, self.getValue() + diff)));
    }
}

}
