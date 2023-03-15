package com.example.demo.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class JobList {

    @NotNull
    private String jobName;
    @NotNull
    private String jobGroup;
    @NotNull
    private String description;
    @NotNull
    private String address;
    @NotNull
    private String type;


    private LocalDateTime localDateTime;


    public JobList(String jobName, String jobGroup, String description, String address, String type) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.description = description;
        this.address = address;
        this.type = type;
    }
}
