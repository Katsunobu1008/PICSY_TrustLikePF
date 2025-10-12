// backend-java/src/main/java/com/picsy/trustlikepf/domain/repository/SettingsRepository.java
package com.picsy.trustlikepf.domain.repository;

import com.picsy.trustlikepf.domain.entity.SettingKV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<SettingKV, String> {
}
