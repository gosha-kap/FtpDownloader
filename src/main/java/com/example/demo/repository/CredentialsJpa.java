package com.example.demo.repository;

import com.example.demo.entity.Credention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CredentialsJpa extends JpaRepository<Credention,Long> {
    @Query(
            value = "SELECT * FROM credention cred WHERE cred.downloadjob_id = :jobKey",
            nativeQuery = true)
    Credention getByJobKey(String jobKey);

    @Override
    Optional<Credention> findById(Long aLong);
}
