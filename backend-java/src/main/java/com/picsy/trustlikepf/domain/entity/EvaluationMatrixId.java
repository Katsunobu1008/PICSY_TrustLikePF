package com.picsy.trustlikepf.domain.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class EvaluationMatrixId implements Serializable {
    @Column(name = "evaluator_id", nullable = false)
    private UUID evaluatorId;

    @Column(name = "evaluatee_id", nullable = false)
    private UUID evaluateeId;

    protected EvaluationMatrixId() {}

    public EvaluationMatrixId(UUID evaluatorId, UUID evaluateeId) {
        this.evaluatorId = evaluatorId;
        this.evaluateeId = evaluateeId;
    }

    public UUID getEvaluatorId() { return evaluatorId; }
    public UUID getEvaluateeId() { return evaluateeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvaluationMatrixId that)) return false;
        return Objects.equals(evaluatorId, that.evaluatorId) &&
               Objects.equals(evaluateeId, that.evaluateeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluatorId, evaluateeId);
    }
}
