package com.picsy.trustlikepf.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.picsy.trustlikepf.domain.entity.Setting;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> { }
