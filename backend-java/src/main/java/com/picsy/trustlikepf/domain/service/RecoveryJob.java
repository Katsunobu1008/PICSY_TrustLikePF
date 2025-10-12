// RecoveryJob.java

package com.picsy.trustlikepf.domain.service;

import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.domain.repository.UserRepository;


@Component
public class RecoveryJob {
    private final SettingsService settings;
    private final EvaluationRowService eService;
    private final UserRepository userRepo;

    public RecoveryJob(SettingsService settings, EvaluationRowService eService, UserRepository userRepo) {
        this.settings = settings;
        this.eService = eService;
        this.userRepo = userRepo;
    }

    // デフォルト1時間。必要に応じて application.properties で picsy.recovery.fixedDelay.ms を調整
    @Scheduled(fixedDelayString = "${picsy.recovery.fixedDelay.ms:3600000}")
    @Transactional
    public void run() {
        double gamma = settings.getGamma();
        for (UUID uid : userRepo.findAll().stream().map(u -> u.getUserId()).toList()) {
            eService.applyRecovery(uid, gamma);
        }
    }
}
