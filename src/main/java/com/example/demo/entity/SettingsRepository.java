package com.example.demo.entity;

import com.example.demo.model.ExSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;



public interface SettingsRepository extends JpaRepository<ExSettings,Long> {

    @Query("select u from ExSettings u where u.jobId = ?1")
    ExSettings findByJobKey (String jobKey);

    void deleteByJobId(String jobkey);

}


