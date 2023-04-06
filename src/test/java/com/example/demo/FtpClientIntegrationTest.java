package com.example.demo;


import com.example.demo.clients.factory.MyClientFactory;
import com.example.demo.dto.CheckResponse;
import com.example.demo.entity.ClientType;
import com.example.demo.entity.Credention;
import com.example.demo.clients.FtpClient;
import com.example.demo.entity.DownloadSettings;
import com.example.demo.entity.FtpSettings;
import com.example.demo.clients.MyClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class FtpClientIntegrationTest {

    private FakeFtpServer fakeFtpServer;
    @Autowired
    MyClientFactory myClientFactory;


    @BeforeAll
    public  void setup() throws IOException {
        fakeFtpServer = new FakeFtpServer();
        Credention credention = new Credention("10.10.10.10","user","password");
        fakeFtpServer.addUserAccount(new UserAccount(credention.getUser(), credention.getPassword(), "/data"));
        UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        TestData.listFiles.forEach(x->fileSystem.add(x));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(21);
        fakeFtpServer.start();

     }


    @Test
    public  void check() throws IOException {
        Credention credention = new Credention("127.0.0.1","user","password");
        credention.setPort(21);
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount(credention.getUser(), credention.getPassword(), "/data"));
        UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        TestData.listFiles.forEach(x->fileSystem.add(x));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(21);
        fakeFtpServer.start();





        FtpSettings settings = new FtpSettings();
        settings.setDataTimeOut(1500);
        settings.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        settings.setFilePostfix("mp3");
        MyClient client = myClientFactory.getService(ClientType.FTP);
        CheckResponse response = client.check(credention,settings);
        System.out.println("----------------------------");
        for (String xml : response.getRecords())
            System.out.println(xml);
        System.out.println("----------------------------");

    }

 }
