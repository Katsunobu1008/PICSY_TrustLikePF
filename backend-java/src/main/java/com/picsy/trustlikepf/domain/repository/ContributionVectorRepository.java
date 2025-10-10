// ContributionVectorRepository.java
package com.picsy.trustlikepf.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.picsy.trustlikepf.domain.entity.ContributionVector;

@Repository
public interface ContributionVectorRepository extends JpaRepository<ContributionVector, UUID> {

    @Query("select c from ContributionVector c order by c.value desc")
    List<ContributionVector> findTopByValue(Pageable pageable);
}
