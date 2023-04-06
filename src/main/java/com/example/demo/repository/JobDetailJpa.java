package com.example.demo.repository;

import com.example.demo.entity.JobDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JobDetailJpa extends JpaRepository<JobDetailEntity,Long> {

    @Query(
            value = "SELECT * FROM job_detail_entity",
            nativeQuery = true)
    List<JobDetailEntity> getAll();

    @Override
    Optional<JobDetailEntity> findById(Long aLong);
}
