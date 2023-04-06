package com.example.demo.repository;

import com.example.demo.entity.FtpSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FtpJpa extends JpaRepository<FtpSettings,Long> {
    @Override
    Optional<FtpSettings> findById(Long aLong);
}
