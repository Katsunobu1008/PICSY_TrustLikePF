// EvaluationRowService.java
package com.picsy.trustlikepf.domain.service;

import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class ERow {
    final UUID evaluator;
    final Map<UUID, EvaluationMatrix> cols = new HashMap<>();
    ERow(UUID evaluator){ this.evaluator = evaluator; }
}

@Service
public class EvaluationRowService {
    private final EvaluationMatrixRepository repo;

    public EvaluationRowService(EvaluationMatrixRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ERow lockAndLoad(UUID evaluatorId, Collection<UUID> ensureCols) {
        List<EvaluationMatrix> list = repo.lockRowByEvaluator(evaluatorId); // 行ロック
        var row = new ERow(evaluatorId);
        list.forEach(e -> row.cols.put(e.getId().getEvaluateeId(), e));
        // 必要キーを存在化（0.0で）
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
