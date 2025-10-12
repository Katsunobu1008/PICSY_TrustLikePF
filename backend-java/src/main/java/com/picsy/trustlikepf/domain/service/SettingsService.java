// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/SettingsService.java
package com.picsy.trustlikepf.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.domain.entity.Setting;
import com.picsy.trustlikepf.domain.repository.SettingRepository;

@Service
public class SettingsService {

    private static final String KEY_GAMMA = "recovery_gamma";
    private static final double DEFAULT_GAMMA = 0.03;

    private final SettingRepository repo;

    public SettingsService(SettingRepository repo){ this.repo = repo; }

    @Transactional(readOnly = true)
    public double getGamma(){
        return repo.findById(KEY_GAMMA)
                .map(s -> parseGamma(s.getValue()))
                .orElse(DEFAULT_GAMMA);
    }

    @Transactional
    public void setGamma(double gamma){
        validateGamma(gamma);
        var s = repo.findById(KEY_GAMMA)
                .orElse(new Setting(KEY_GAMMA, String.valueOf(gamma)));
        s.setValue(String.valueOf(gamma));
        repo.save(s);
    }

    private static void validateGamma(double gamma){
        if (gamma <= 0 || gamma >= 1) {
            throw new IllegalArgumentException("gamma must be in (0,1)");
        }
    }
    private static double parseGamma(String s){
        try { return Double.parseDouble(s); }
        catch (Exception e){ return DEFAULT_GAMMA; }
    }
}
