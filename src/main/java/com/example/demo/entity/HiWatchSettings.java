package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
public class HiWatchSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int channel = 101;
    private int searchMaxResult =50 ;
    private int searchResultPosition =0 ;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private boolean timeShift;

    public HiWatchSettings(int channel, LocalDateTime from, LocalDateTime to, boolean timeShift) {
        this.channel = channel;
        this.fromTime = from;
        this.toTime = to;
        this.timeShift = timeShift;
    }

    public HiWatchSettings(LocalDateTime from, LocalDateTime to, boolean timeShift) {
        this.fromTime = from;
        this.toTime = to;
        this.timeShift = timeShift;
    }

    public HiWatchSettings(int channel, boolean timeShift) {
        this.channel = channel;
        this.timeShift = timeShift;
    }
}
