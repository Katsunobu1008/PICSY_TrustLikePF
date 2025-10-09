// backend-java/src/main/java/com/picsy/trustlikepf/domain/repository/UserRepository.java
package com.picsy.trustlikepf.domain.repository;

import com.picsy.trustlikepf.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {}
