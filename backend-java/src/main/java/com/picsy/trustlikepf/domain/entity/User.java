// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/User.java
package com.picsy.trustlikepf.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="users")
public class User {
    @Id
    @Column(name="user_id")
    private UUID userId;

    @Column(name="name", nullable=false, length=50)
    private String name;

    @Column(name="commission_rate", nullable=false, precision=5, scale=4)
    private BigDecimal commissionRate;

    protected User(){}

    public UUID getUserId(){ return userId; }
    public String getName(){ return name; }
    public BigDecimal getCommissionRate(){ return commissionRate; }
}
