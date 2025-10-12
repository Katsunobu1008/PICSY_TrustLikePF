// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/User.java
// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/User.java
package com.picsy.trustlikepf.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(name="is_active", nullable=false)
    private boolean isActive = true; // ★ 既存列のマッピングを追加



    protected User(){}

    public UUID getUserId(){ return userId; }
    public String getName(){ return name; }
    public BigDecimal getCommissionRate(){ return commissionRate; }
    public boolean isActive(){ return isActive; }          // ★ 追加
    public void setActive(boolean active){ this.isActive = active; } // ★ 追加
}
