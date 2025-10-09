// EvaluationMatrixRepository.java
package com.picsy.trustlikepf.domain.repository;

import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrixId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.*;

@Repository
public interface EvaluationMatrixRepository extends JpaRepository<EvaluationMatrix, EvaluationMatrixId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EvaluationMatrix e where e.id.evaluatorId = :evaluatorId")
    List<EvaluationMatrix> lockRowByEvaluator(@Param("evaluatorId") UUID evaluatorId);

    @Query("select e from EvaluationMatrix e where e.id.evaluatorId = :evaluatorId")
    List<EvaluationMatrix> findByEvaluator(@Param("evaluatorId") UUID evaluatorId);

    @Query("select e from EvaluationMatrix e where e.id.evaluateeId = :evaluateeId")
    List<EvaluationMatrix> findByEvaluatee(@Param("evaluateeId") UUID evaluateeId);
}
