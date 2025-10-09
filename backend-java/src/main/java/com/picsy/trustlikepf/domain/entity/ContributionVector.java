// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/ContributionVector.java
package com.picsy.trustlikepf.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="contribution_vector")
public class ContributionVector {
    @Id
    @Column(name="user_id", nullable=false)
    private UUID userId;

    @Column(name="value", nullable=false)
    private double value;

    protected ContributionVector() {}

    public ContributionVector(UUID userId, double value) {
        this.userId = userId;
        this.value = value;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
