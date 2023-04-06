package com.example.demo.repository;

import com.example.demo.entity.DownloadSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DownloadSettingsJpa extends JpaRepository<DownloadSettings,Long> {

    @Override
    Optional<DownloadSettings> findById(Long aLong);
}
