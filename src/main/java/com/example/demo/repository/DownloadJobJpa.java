package com.example.demo.repository;

import com.example.demo.entity.DownloadJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DownloadJobJpa extends JpaRepository<DownloadJobEntity,Long> {

    @Query(
            value = "select * from download_job_entity dwn where dwn.job_key =:jobId",
            nativeQuery = true)
    DownloadJobEntity getByJobId(@Param("jobId") String jobId);

    void deleteByJobKey(String jobkey);

    @Override
    Optional<DownloadJobEntity> findById(Long aLong);
}
