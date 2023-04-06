package com.example.demo.repository;

import com.example.demo.entity.HiWatchSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HIWatchJPA extends JpaRepository<HiWatchSettings,Long> {

    @Override
    Optional<HiWatchSettings> findById(Long aLong);
}
