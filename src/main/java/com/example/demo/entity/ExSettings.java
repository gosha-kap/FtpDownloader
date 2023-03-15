package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class ExSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String jobId;
    @Column
    private Boolean repeatLater;
    @Column
    private Long nextTimeRun;
    @Column
    private Integer numOfRepeats;

    public ExSettings() {
    }

    public ExSettings(Long id, String jobId, Boolean repeatLater, Long nextTimeRun, Integer numOfRepeats) {
        this.id = id;
        this.jobId = jobId;
        this.repeatLater = repeatLater;
        this.nextTimeRun = nextTimeRun;
        this.numOfRepeats = numOfRepeats;
    }

    public ExSettings(String jobId, Boolean repeatLater, Long nextTimeRun, Integer numOfRepeats) {
        this.jobId = jobId;
        this.repeatLater = repeatLater;
        this.nextTimeRun = nextTimeRun;
        this.numOfRepeats = numOfRepeats;
    }

    public ExSettings(Boolean repeatLater, Long nextTimeRun, Integer numOfRepeats) {
        this.repeatLater = repeatLater;
        this.nextTimeRun = nextTimeRun;
        this.numOfRepeats = numOfRepeats;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Boolean getRepeatLater() {
        return repeatLater;
    }

    public void setRepeatLater(Boolean repeatLater) {
        this.repeatLater = repeatLater;
    }

    public Long getNextTimeRun() {
        return nextTimeRun;
    }

    public void setNextTimeRun(Long nextTimeRun) {
        this.nextTimeRun = nextTimeRun;
    }

    public Integer getNumOfRepeats() {
        return numOfRepeats;
    }

    public void setNumOfRepeats(Integer numOfRepeats) {
        this.numOfRepeats = numOfRepeats;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void useOneRepeat() {
        if(numOfRepeats>0){
            numOfRepeats--;
        }
        if(numOfRepeats == 0){
            repeatLater = false;
        }

    }
}
