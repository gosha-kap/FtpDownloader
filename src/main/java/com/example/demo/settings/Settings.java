package com.example.demo.settings;


import lombok.Data;

import java.io.Serializable;

@Data
public abstract class Settings implements Serializable {


    private String saveFolder;
    private int numOfTries = 1;
    private TelegramCredention telegramCredention;

    public boolean continueRepeat() {
        return numOfTries > 0;
    }

    public void oneMoreTry() {
        numOfTries--;
    }

}
