// backend-java/src/main/java/com/picsy/trustlikepf/api/DashboardController.java
package com.picsy.trustlikepf.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picsy.trustlikepf.domain.entity.ContributionVector;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final ContributionVectorRepository cRepo;
    private final EvaluationMatrixRepository eRepo;

    public DashboardController(ContributionVectorRepository cRepo, EvaluationMatrixRepository eRepo) {
        this.cRepo = cRepo; this.eRepo = eRepo;
    }

    @GetMapping("/core-metrics/{userId}")
    public Map<String,Object> core(@PathVariable UUID userId){
        double c = cRepo.findById(userId).map(ContributionVector::getValue).orElse(1.0);
        double eii = eRepo.findByEvaluator(userId).stream()
                .filter(em -> em.getId().getEvaluateeId().equals(userId))
                .mapToDouble(em -> em.getValue()).findFirst().orElse(1.0);
        double purchasing = eii * c;
        return Map.of("ok", true, "c", c, "Eii", eii, "purchasingPower", purchasing);
    }
}
