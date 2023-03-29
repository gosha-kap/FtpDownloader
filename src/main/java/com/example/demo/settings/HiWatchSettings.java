package com.example.demo.settings;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HiWatchSettings extends Settings{

    private int channel = 101;
    private int searchMaxResult = 50;
    private int searchResultPosition = 0;
    private LocalDateTime from;
    private LocalDateTime to;
    private boolean timeShift;

    public HiWatchSettings(int channel, LocalDateTime from, LocalDateTime to, boolean timeShift) {
        this.channel = channel;
        this.from = from;
        this.to = to;
        this.timeShift = timeShift;
    }

    public HiWatchSettings(LocalDateTime from, LocalDateTime to, boolean timeShift) {
        this.from = from;
        this.to = to;
        this.timeShift = timeShift;
    }

    public HiWatchSettings(int channel, boolean timeShift) {
        this.channel = channel;
        this.timeShift = timeShift;
    }
}
