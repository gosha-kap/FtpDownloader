package com.example.demo.settings;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HiWatchSettings extends Settings{

    private int channel = 101;
    private int searchMaxResult = 50;
    private int searchResultPosition = 0;
    private LocalDateTime from;
    private LocalDateTime to;
    private boolean timeShift;

}
