// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/Setting.java
package com.picsy.trustlikepf.domain.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "settings")
public class Setting {
    @Id
    @Column(name="key", nullable=false)
    private String key;

    @Column(name="value", nullable=false)
    private String value;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    public Setting(){}

    public Setting(String key, String value){
        this.key = key;
        this.value = value;
        this.updatedAt = Instant.now();
    }

    @PrePersist @PreUpdate
    public void touch(){ this.updatedAt = Instant.now(); }

    public String getKey(){ return key; }
    public String getValue(){ return value; }
    public Instant getUpdatedAt(){ return updatedAt; }

    public void setValue(String value){ this.value = value; }
}
