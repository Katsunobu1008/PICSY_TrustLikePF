// AdminController.java
package com.picsy.trustlikepf.api;

import com.picsy.trustlikepf.domain.service.PicsyEngine;
import com.picsy.trustlikepf.domain.service.RecoveryJob;
import com.picsy.trustlikepf.domain.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> getGamma() {
        return ResponseEntity.ok().body(new Gamma(settings.getGamma()));
    }

    @PutMapping("/settings/recovery-gamma")
    public ResponseEntity<?> setGamma(@RequestBody Gamma req) {
        settings.setGamma(req.value());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recover")
    public ResponseEntity<?> runRecoveryOnce() {
        recoveryJob.run();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/recalc-c")
    public ResponseEntity<?> recalcC() {
        engine.recalcC();
        return ResponseEntity.accepted().build();
    }

    public record Gamma(double value) {}
}
