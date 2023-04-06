package com.example.demo.repository;

import com.example.demo.entity.TelegramCredention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramJpa extends JpaRepository<TelegramCredention,Long> {
    @Override
    Optional<TelegramCredention> findById(Long aLong);
}
