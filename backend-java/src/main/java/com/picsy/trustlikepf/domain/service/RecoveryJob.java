// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/RecoveryJob.java
package com.picsy.trustlikepf.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.domain.repository.UserRepository;

@Service
public class RecoveryJob {

    private final SettingsService settings;
    private final EvaluationRowService eService;
    private final UserRepository users;

    public RecoveryJob(SettingsService settings, EvaluationRowService eService, UserRepository users) {
        this.settings = settings;
        this.eService = eService;
        this.users = users;
    }

    /** γ を現在値で一括適用（アクティブユーザーのみ） */
    @Transactional
    public void run(){
        double gamma = settings.getGamma();
        if (gamma <= 0.0) return;
        users.findAllActive().forEach(u -> eService.applyRecovery(u.getUserId(), gamma));
    }
}
