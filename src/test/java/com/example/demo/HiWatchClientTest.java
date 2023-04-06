package com.example.demo;

import com.example.demo.clients.MyClient;
import com.example.demo.clients.factory.MyClientFactory;
import com.example.demo.dto.CheckResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.DownloadJobJpa;
import com.example.demo.service.HIWatchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class HiWatchClientTest {
    @Autowired
    MyClientFactory myClientFactory;

    @Autowired
    DownloadJobJpa downloadJobJpa;

    @Autowired
    HIWatchService hiWatchService;

    @Test
    public void download() throws IOException {

        Credention credention = new Credention("192.168.100.16", "admin", "Gosh@183");
        credention.setPort(80);
        HiWatchSettings settings = new HiWatchSettings();
        settings.setChannel(201);
        settings.setSearchMaxResult(50);
        settings.setSearchResultPosition(0);
        settings.setTimeShift(true);
        MyClient client = myClientFactory.getService(ClientType.HiWatch);
        CheckResponse response = client.check(credention,settings);
        System.out.println("----------------------------");
        for (String xml : response.getRecords())
            System.out.println(xml);
        System.out.println("----------------------------");
    }

    @Test
    public void save() throws IOException{
        Credention credention = new Credention("10.1.1.1","user","Pass");
        HiWatchSettings settings = new HiWatchSettings();
        settings.setTimeShift(true);
        DownloadSettings downloadSettings = new DownloadSettings();
        downloadSettings.setSaveFolder(TestData.saveFolder);
        JobDetailEntity jobDetail = new JobDetailEntity();
        jobDetail.setAlias("Test");
        jobDetail.setType(ClientType.HiWatch);
        DownloadJobEntity downloadJobEntity = new DownloadJobEntity();
        downloadJobEntity.setJobKey("TEST_KEY");
        downloadJobEntity.setDownloadSettings(downloadSettings);
        downloadJobEntity.setCredention(credention);
        downloadJobEntity.setHiWatchSettings(settings);
        downloadJobEntity.setJobDetailEntity(jobDetail);
        downloadJobJpa.save(downloadJobEntity);
        System.out.println();

    }



}
