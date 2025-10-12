// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/EvaluationRowService.java
package com.picsy.trustlikepf.domain.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;

@Service
public class EvaluationRowService {

public static class Row {
    private final UUID evaluator;
    private final Map<UUID, EvaluationMatrix> cols = new HashMap<>();
    Row(UUID evaluator){ this.evaluator = evaluator; }
    public UUID evaluator(){ return evaluator; }
    public Map<UUID, EvaluationMatrix> cols(){ return cols; } // ★アクセサ
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
        // 必要キーを存在化（0.0で）
        for (UUID colId : ensureCols) {
            row.cols.computeIfAbsent(colId, cid -> repo.save(
                    new EvaluationMatrix(evaluatorId, cid, 0.0)
            ));
        }
        return row;
    }

    /** 値を加算（負値禁止, 6桁丸め） */
    public void add(EvaluationMatrix em, double delta){
        double v = em.getValue() + delta;
        if (v < 0) v = 0;
        em.setValue(round6(v));
    }

    static double round6(double x){ return Math.round(x * 1_000_000d)/1_000_000d; }

    /** 自然回収の1行適用：非対角を(1-γ)倍、削った質量を対角へ戻す。最後に行和=1へ微調整。 */
   @Transactional
    public void applyRecovery(UUID evaluatorId, double gamma){
        if (gamma <= 0 || gamma >= 1) return;

        var row = lockAndLoad(evaluatorId, Set.of(evaluatorId));

        EvaluationMatrix diag = row.cols.get(evaluatorId);
        if (diag == null) {
            diag = repo.save(new EvaluationMatrix(evaluatorId, evaluatorId, 0.0));
            row.cols.put(evaluatorId, diag);
        }
        double eii = diag.getValue();   // ★ ここで初期化

        // 非対角を(1-γ)倍
        for (var entry : row.cols.entrySet()){
            UUID j = entry.getKey();
            var em = entry.getValue();
            if (j.equals(evaluatorId)) continue;
            double v = em.getValue();
            double nv = round6(v * (1.0 - gamma));
            em.setValue(nv);
        }

        // 削った総量 γ*(1 - Eii) を対角へ戻す
        double delta = gamma * (1.0 - eii);
        diag.setValue(round6(eii + delta));

        // 丸め誤差の微調整
        double sum = 0.0;
        for (var em : row.cols.values()) sum += em.getValue();
        double eps = round6(1.0 - sum);
        if (Math.abs(eps) > 1e-9) {
            diag.setValue(round6(diag.getValue() + eps));
            if (diag.getValue() < 0) diag.setValue(0.0);
        }
    }
}
