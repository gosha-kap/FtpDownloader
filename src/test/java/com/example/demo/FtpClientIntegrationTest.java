package com.example.demo;


import com.example.demo.clients.Credention;
import com.example.demo.clients.FtpClient;
import com.example.demo.clients.FtpSettings;
import com.example.demo.clients.MyClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FtpClientIntegrationTest {

    private FakeFtpServer fakeFtpServer;
    private MyClient ftpConnection;

    @BeforeEach
    public  void setup() throws IOException {
        fakeFtpServer = new FakeFtpServer();
        Credention credention = new Credention("localhost","user","password");

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
        settings.setSaveFolder(TestData.saveFolder);
        settings.setFilePostfix("mp3");

        ftpConnection = new FtpClient(credention,settings);
        ftpConnection.connect();
    }

    @AfterEach
    public void teardown() throws IOException {
        ftpConnection.close();
        fakeFtpServer.stop();
    }

    @Test
    public void getListFiles(){
        List<FTPFile> files = ftpConnection.getFilesFromRoot();
                  assertThat(files.size() == 6);
    }

    @Test
    public void downLoadAndDelete() throws IOException {
        ftpConnection.downLoad();
        List<FTPFile> files = ftpConnection.getFilesFromRoot();
        assertThat(files.size() == 3);
    }

    @Test
    public void downloadfolders(){

    }
 }
