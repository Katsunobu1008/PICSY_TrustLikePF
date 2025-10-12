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
}
