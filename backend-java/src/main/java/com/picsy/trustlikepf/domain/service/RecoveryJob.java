// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/RecoveryJob.java
package com.picsy.trustlikepf.domain.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.picsy.trustlikepf.domain.entity.User;
import com.picsy.trustlikepf.domain.repository.UserRepository;

@Service
public class RecoveryJob {

    private final SettingsService settings;
    private final EvaluationRowService eService;
    private final UserRepository users;

    @Value("${picsy.recovery.enabled:true}")
    private boolean enabled;

    public RecoveryJob(SettingsService settings, EvaluationRowService eService, UserRepository users) {
        this.settings = settings;
        this.eService = eService;
        this.users = users;
    }

    // デフォルト 1時間おき。enabled=false なら即 return。
    @Scheduled(fixedDelayString = "${picsy.recovery.fixedDelay.ms:3600000}")
    public void scheduled(){
        if (!enabled) return;
        run();
    }

    /** 全ユーザーに自然回収を1回適用（手動実行でも使用） */
    public void run(){
        double gamma = settings.getGamma();
        List<UUID> ids = users.findAll().stream().map(User::getUserId).toList();
        for (UUID id : ids){
            eService.applyRecovery(id, gamma);
        }
    }
}
