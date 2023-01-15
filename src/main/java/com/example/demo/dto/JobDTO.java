package com.example.demo.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class JobDTO {

    @NotNull
    private String jobName;

    @NotNull
    private String jobGroup;

    private String description;

    public JobDTO(String jobName, String jobGroup, String description) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.description = description;
    }

    public JobDTO(String jobName, String jobGroup) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
    }

    private LocalDateTime localDateTime;
}
