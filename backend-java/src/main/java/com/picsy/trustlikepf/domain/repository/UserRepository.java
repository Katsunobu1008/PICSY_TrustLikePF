// backend-java/src/main/java/com/picsy/trustlikepf/domain/repository/UserRepository.java
package com.picsy.trustlikepf.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.picsy.trustlikepf.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ★ Spring Data の命名規約でアクティブのみ取得
    List<User> findByIsActiveTrue();
}
