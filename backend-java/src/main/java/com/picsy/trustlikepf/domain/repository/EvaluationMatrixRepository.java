// backend-java/src/main/java/com/picsy/trustlikepf/domain/repository/EvaluationMatrixRepository.java
package com.picsy.trustlikepf.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    // ★ 追加：行削除（評価者=ユーザー）
    @Modifying
    @Query("delete from EvaluationMatrix e where e.id.evaluatorId = :evaluatorId")
    void deleteByEvaluator(@Param("evaluatorId") UUID evaluatorId);

    // ★ 追加：列削除（被評価者=ユーザー）
    @Modifying
    @Query("delete from EvaluationMatrix e where e.id.evaluateeId = :evaluateeId")
    void deleteByEvaluatee(@Param("evaluateeId") UUID evaluateeId);
}
