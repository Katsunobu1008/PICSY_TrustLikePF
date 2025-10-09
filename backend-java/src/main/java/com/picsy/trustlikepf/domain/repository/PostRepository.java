// backend-java/src/main/java/com/picsy/trustlikepf/domain/repository/PostRepository.java
package com.picsy.trustlikepf.domain.repository;

import com.picsy.trustlikepf.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {}
