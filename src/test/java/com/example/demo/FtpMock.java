package com.example.demo;

import com.example.demo.entity.Credention;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

public class FtpMock {
    public static void main(String[] args) {
        Credention credention = new Credention("127.0.0.1","user","password");
        credention.setPort(21);
        FakeFtpServer fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount(credention.getUser(), credention.getPassword(), "/data"));
        UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        TestData.listFiles.forEach(x->fileSystem.add(x));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(21);
        fakeFtpServer.start();
    }
}
