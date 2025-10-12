// backend-java/src/main/java/com/picsy/trustlikepf/domain/repository/UserRepository.java
package com.picsy.trustlikepf.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.picsy.trustlikepf.domain.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("select u from User u where u.isActive = true")
    List<User> findAllActive(); // ★ 追加
// backend-java/src/main/java/com/picsy/trustlikepf/domain/repository/UserRepository.java
List<com.picsy.trustlikepf.domain.entity.User> findByIsActiveTrue();

}
