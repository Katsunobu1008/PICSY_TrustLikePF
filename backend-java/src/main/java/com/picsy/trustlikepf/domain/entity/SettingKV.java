// backend-java/src/main/java/com/picsy/trustlikepf/domain/entity/SettingKV.java
package com.picsy.trustlikepf.domain.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "settings")
public class SettingKV {

    @Id
    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SettingKV(){}

    public SettingKV(String key, String value){
        this.key = key;
        this.value = value;
        this.updatedAt = Instant.now();
    }

    public String getKey(){ return key; }
    public String getValue(){ return value; }
    public Instant getUpdatedAt(){ return updatedAt; }

    public void setKey(String key){ this.key = key; }
    public void setValue(String value){ this.value = value; }
    public void setUpdatedAt(Instant updatedAt){ this.updatedAt = updatedAt; }
}
