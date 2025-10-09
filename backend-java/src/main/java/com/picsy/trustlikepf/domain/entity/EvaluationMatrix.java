package com.picsy.trustlikepf.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "evaluation_matrix")
public class EvaluationMatrix {
    @EmbeddedId
    private EvaluationMatrixId id;

    @Column(name="value", nullable=false)
    private double value;

    protected EvaluationMatrix() {}

    public EvaluationMatrix(UUID evaluatorId, UUID evaluateeId, double value) {
        this.id = new EvaluationMatrixId(evaluatorId, evaluateeId);
        this.value = value;
    }

    public EvaluationMatrixId getId() { return id; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
