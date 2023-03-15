package com.example.demo;

import com.example.demo.clients.HiWatchClient;
import com.example.demo.clients.MyClient;
import com.example.demo.settings.Credention;
import com.example.demo.settings.HiWatchSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HiWatchClientTest {
    private MyClient myClient;

    @BeforeEach
    public void setup() throws IOException {

        Credention credention = new Credention("192.168.100.16", "admin", "Gosh@183");
        HiWatchSettings settings = new HiWatchSettings();
        settings.setSaveFolder(TestData.saveFolder);
        settings.setChannel(201);
        settings.setFrom(LocalDateTime.now().minusHours(24));
        settings.setTo(LocalDateTime.now());
        settings.setSearchMaxResult(50);
        myClient = new HiWatchClient(credention, settings);
    }

    @Test
    public void getListOfRecords() throws IOException {
        List<String> list = myClient.getFilesFromRoot();
        for (String str : list) {
            Pattern pattern = Pattern.compile("starttime=(\\d{8})T(\\d{6})Z.*name=(\\d*).*size=(\\d*)");
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                String date = matcher.group(1);
                String time = matcher.group(2);
                String name = matcher.group(3);
                String size = matcher.group(4);
                String folderName = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
                System.out.println(date + "-" + time + ":" + size);
                System.out.println("file :"+name);
            }
        }
    }

    @Test
    public void getListOfRecordsNo() throws IOException {
       myClient.downLoad();
    }

}
