package com.example.demo.clients;


import com.example.demo.dto.CheckResponse;
import com.example.demo.dto.CheckStatus;
import com.example.demo.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FtpClient implements MyClient{

    private FTPClient ftp = new FTPClient();
    private String workDirectory;

    public void connect(Credention credention,FtpSettings settings) throws IOException {
        ftp.setConnectTimeout(settings.getDataTimeOut());
        ftp.connect(credention.getServer(), credention.getPort());
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            log.error("Exception in connecting to FTP Server: " + reply + " code.");
        }
        ftp.login(credention.getUser(), credention.getPassword());
        ftp.enterLocalPassiveMode();
        ftp.setFileType(settings.getFileType());
        this.workDirectory = ftp.printWorkingDirectory();
    }
    @Override
    public void downLoad(Credention credention, String saveFolder, Object settings) throws IOException {
        FtpSettings ftpSettings = null;
        try{
            ftpSettings = (FtpSettings)settings;
        }
        catch (ClassCastException e){
            log.error("Can't get Ftp Settings");
            throw  new RuntimeException("Can't get Ftp Settings");
        }
        connect(credention,ftpSettings);
        List<FTPFile> folders = getFilesFromRoot();
        for (FTPFile folder : folders) {
            log.info("Process folder: "+folder.getName()+".");
            if (folder.isDirectory()) {
                List<FTPFile> files = getFilesFromPath(workDirectory.concat("/").concat(folder.getName()));
                for (FTPFile file : files) {
                    if (file.getName().endsWith(ftpSettings.getFilePostfix()))
                        downLoadFile(folder.getName(), file ,saveFolder);
                }
            }
        }
        close();
    }

    @Override
    public ClientType getType() {
        return ClientType.FTP;
    }

    @Override
    public CheckResponse check(Credention credention , Object settings) {
        CheckResponse checkResponse = new CheckResponse();
        List<String> records = new ArrayList<>();
        FtpSettings ftpSettings = null;
        try{
            ftpSettings = (FtpSettings)settings;
        }
        catch (ClassCastException e){
            log.error("Can't get Ftp Settings");
            checkResponse.setCheckStatus(CheckStatus.ERROR);
            checkResponse.setMessadge("Can't get Ftp Settings");
        }
        try {
            connect(credention,ftpSettings);
            List<FTPFile> folders = getFilesFromRoot();

            for (FTPFile folder : folders) {

                if (folder.isDirectory()) {
                    records.add("DIR --- "+folder.getName());
                    List<FTPFile> files = getFilesFromPath(workDirectory.concat("/").concat(folder.getName()));
                    for (FTPFile file : files) {
                        records.add("FILE  --- "+file.getName()+", size = "+file.getSize()+" kByte.");
                    }
                }
            }
            close();
            checkResponse.setRecords(records);
            checkResponse.setCheckStatus(CheckStatus.OK);
            return  checkResponse;

        } catch (IOException e) {
            checkResponse.setCheckStatus(CheckStatus.ERROR);
            checkResponse.setMessadge(e.getMessage());
            return  checkResponse;
        }

    }

    public List<FTPFile> getFilesFromPath(String dir) {
        FTPFile[] files;
        try {
            files = ftp.listFiles(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.stream(files).collect(Collectors.toList());
    }

    public List<FTPFile> getFilesFromRoot() {
        return getFilesFromPath(workDirectory);
    }

    public void downLoadFile(String proccessFolder, FTPFile pathFile, String saveFolder) throws IOException {

        File localFolder = new File(saveFolder.concat("/").concat(proccessFolder));
        File localFile = new File(saveFolder.concat("/").concat(proccessFolder).concat("/").concat(pathFile.getName()));
        String remotePathFile = workDirectory.concat("/").concat(proccessFolder).concat("/").concat(pathFile.getName());
        log.info("Process file :"+pathFile.getName());

        if (!localFolder.exists())
            localFolder.mkdir();
        boolean exists = localFile.exists();
        long length = localFile.length();
        log.info("Remote file size: "+pathFile.getSize());
        if (exists && length == pathFile.getSize()) {
            log.info("File is already exists.");
            deleteFile(remotePathFile,proccessFolder);
            return;
        } else if (exists) {
            log.info("File is continued to downloading. Local file size:"+ (int)(length/pathFile.getSize()*100)+"%.");
            ftp.setRestartOffset(length);
        } else {
            if (!localFile.createNewFile())
                log.error("Can't create file");

        }
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile, exists));
        InputStream inputStream = ftp.retrieveFileStream(remotePathFile);

        byte[] bytesArray = new byte[4096];
        int bytesRead;

        while (ftp.isConnected() && (bytesRead = inputStream.read(bytesArray)) != -1) {
            outputStream.write(bytesArray, 0, bytesRead);
        }
        if (ftp.isConnected() && ftp.completePendingCommand()) {
            outputStream.close();
            inputStream.close();

            if (localFile.length() == pathFile.getSize()) {
                log.info("Downloading file completed.");
                deleteFile(remotePathFile,proccessFolder);

            } else
                log.error("File error downloading: after downloading size is not coinside.");
        } else {
            log.error("File error downloading: compliting command wrong");
            outputStream.close();
            inputStream.close();
        }
    }

    public void deleteFile(String remotePathFile, String proccessFolder) throws IOException {
        log.info("Deleting file: "+remotePathFile+".");
        ftp.deleteFile(remotePathFile);
        if (ftp.listFiles(workDirectory.concat("/").concat(proccessFolder)).length == 0){
            log.info("Removing folder "+proccessFolder+" from "+workDirectory+".");
            ftp.removeDirectory(workDirectory.concat("/").concat(proccessFolder));
        }
    }

    public void close() throws IOException {

        if (ftp.isConnected()) {
            ftp.logout();
            ftp.disconnect();
        }
    }



}
