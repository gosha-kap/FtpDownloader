package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class DownloadSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String saveFolder;
    private int numOfTries  = 1;
    private Boolean repeatLater;
    private Long nextTimeRun;
    private Integer numOfRepeats;

    public DownloadSettings(Long id, Boolean repeatLater, Long nextTimeRun, Integer numOfRepeats) {
        this.id = id;
        this.repeatLater = repeatLater;
        this.nextTimeRun = nextTimeRun;
        this.numOfRepeats = numOfRepeats;
    }

    public DownloadSettings(Boolean repeatLater, Long nextTimeRun, Integer numOfRepeats) {

        this.repeatLater = repeatLater;
        this.nextTimeRun = nextTimeRun;
        this.numOfRepeats = numOfRepeats;
    }




}
