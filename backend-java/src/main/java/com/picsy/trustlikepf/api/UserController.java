// UserController.java
package com.picsy.trustlikepf.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picsy.trustlikepf.domain.entity.ContributionVector;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ContributionVectorRepository cRepo;
    private final EvaluationMatrixRepository eRepo;
    private final UserRepository userRepo;

    public UserController(ContributionVectorRepository cRepo,
                          EvaluationMatrixRepository eRepo,
                          UserRepository userRepo) {
        this.cRepo = cRepo;
        this.eRepo = eRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/{userId}/power")
    public ResponseEntity<?> power(@PathVariable UUID userId){
        userRepo.findById(userId).orElseThrow(); // 404 if not found

        double c = cRepo.findById(userId).map(ContributionVector::getValue).orElse(1.0);
        double eii = eRepo.findById(new com.picsy.trustlikepf.domain.entity.EvaluationMatrixId(userId, userId))
                          .map(EvaluationMatrix::getValue).orElse(0.0);
        double purchasingPower = eii * c;
        return ResponseEntity.ok(new Power(eii, c, purchasingPower));
    }

    public record Power(double eii, double c, double purchasingPower) {}
}
