// backend-java/src/main/java/com/picsy/trustlikepf/api/AdminController.java
package com.picsy.trustlikepf.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picsy.trustlikepf.domain.service.PicsyEngine;
import com.picsy.trustlikepf.domain.service.RecoveryJob;
import com.picsy.trustlikepf.domain.service.SettingsService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final SettingsService settings;
    private final RecoveryJob recoveryJob;
    private final PicsyEngine engine;

    public AdminController(SettingsService settings, RecoveryJob recoveryJob, PicsyEngine engine) {
        this.settings = settings;
        this.recoveryJob = recoveryJob;
        this.engine = engine;
    }

    @GetMapping("/settings/recovery-gamma")
    public ResponseEntity<Gamma> getGamma() {
        return ResponseEntity.ok(new Gamma(settings.getGamma())); // ← ok().body(...) でなく ok(...)
    }

    @PutMapping("/settings/recovery-gamma")
    public ResponseEntity<Void> setGamma(@RequestBody Gamma req) {
        settings.setGamma(req.value());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recover")
    public ResponseEntity<Void> runRecoveryOnce() {
        recoveryJob.run();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/recalc-c")
    public ResponseEntity<Void> recalcC() {
        engine.recalcC();
        return ResponseEntity.accepted().build();
    }

    public record Gamma(double value) {}
}
