// backend-java/src/main/java/com/picsy/trustlikepf/api/UserAdminController.java
package com.picsy.trustlikepf.api;

import com.picsy.trustlikepf.domain.entity.ContributionVector;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrixId;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {
    private final UserRepository userRepo;
    private final EvaluationMatrixRepository eRepo;
    private final ContributionVectorRepository cRepo;

    public UserAdminController(UserRepository userRepo, EvaluationMatrixRepository eRepo, ContributionVectorRepository cRepo) {
        this.userRepo = userRepo;
        this.eRepo = eRepo;
        this.cRepo = cRepo;
    }

    /** 退会＝凍結。Eの行/列とcを削除し、以後の取引を拒否 */
    @PostMapping("/{userId}/freeze")
    @Transactional
    public ResponseEntity<Void> freeze(@PathVariable UUID userId){
        var u = userRepo.findById(userId).orElseThrow();
        if (!u.isActive()) return ResponseEntity.noContent().build();

        // Eの行・列を削除、cも削除
        eRepo.deleteByEvaluator(userId);
        eRepo.deleteByEvaluatee(userId);
        cRepo.deleteById(userId);

        u.setActive(false);
        userRepo.save(u);
        return ResponseEntity.noContent().build();
    }

    /** 解凍。c=1.0 を初期投入、E_ii=1.0 */
    @PostMapping("/{userId}/reactivate")
    @Transactional
    public ResponseEntity<Void> reactivate(@PathVariable UUID userId){
        var u = userRepo.findById(userId).orElseThrow();
        if (!u.isActive()) {
            u.setActive(true);
            userRepo.save(u);
        }
        // cを初期化
        cRepo.save(new ContributionVector(userId, 1.0));
        // E_ii が無ければ作成
        var key = new EvaluationMatrixId(userId, userId);
        if (eRepo.findById(key).isEmpty()){
            eRepo.save(new EvaluationMatrix(userId, userId, 1.0));
        }
        return ResponseEntity.noContent().build();
    }
}
