// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/SettingsService.java
package com.picsy.trustlikepf.domain.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.domain.entity.SettingKV;
import com.picsy.trustlikepf.domain.repository.SettingsRepository;

@Service
public class SettingsService {

    private final SettingsRepository repo;

    public SettingsService(SettingsRepository repo) {
        this.repo = repo;
    }

    public double getGamma() {
        return repo.findById("recovery_gamma")
                .map(s -> Double.parseDouble(s.getValue()))
                .orElse(0.03); // マイグレーションの初期値と一致
    }

    @Transactional
    public void setGamma(double gamma) {
        if (gamma < 0.0 || gamma > 1.0) {
            throw new IllegalArgumentException("gamma must be in [0,1]");
        }
        SettingKV kv = repo.findById("recovery_gamma")
                .orElse(new SettingKV("recovery_gamma", String.valueOf(gamma)));
        kv.setValue(String.valueOf(gamma));
        kv.setUpdatedAt(Instant.now());
        repo.save(kv);
    }
}
