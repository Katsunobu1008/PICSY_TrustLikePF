// EvaluationMatrixRepository.java
package com.picsy.trustlikepf.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrixId;

import jakarta.persistence.LockModeType;

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
