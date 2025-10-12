package com.picsy.trustlikepf.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.domain.entity.Setting;
import com.picsy.trustlikepf.domain.repository.SettingRepository;

@Service
public class SettingsService {
    private final SettingRepository repo;

    @Value("${picsy.recovery.gamma.default:0.03}")
    private double defaultGamma;

    public SettingsService(SettingRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public double getGamma() {
        return repo.findById("recovery_gamma")
                   .map(s -> parseOrDefault(s.getValue(), defaultGamma))
                   .orElse(defaultGamma);
    }

    @Transactional
    public void setGamma(double gamma) {
        if (gamma <= 0 || gamma >= 1) {
            throw new IllegalArgumentException("gamma must be in (0,1)");
        }
        var s = repo.findById("recovery_gamma")
                    .orElse(new Setting("recovery_gamma", Double.toString(gamma)));
        s.setValue(Double.toString(gamma));
        repo.save(s);
    }

    private static double parseOrDefault(String v, double def){
        try { return Double.parseDouble(v); } catch (Exception e){ return def; }
    }
}
