package com.example.demo.clients;


import com.example.demo.Jobs.DownloadJob;
import com.example.demo.settings.Credention;
import com.example.demo.settings.FtpSettings;
import com.example.demo.settings.Settings;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class FtpClient implements MyClient<FTPFile> {

    private Credention credention;
    private FTPClient ftp;
    private FtpSettings settings;
    private String saveFolder;
    private String workDirectory;
    private String filePostfix;

    private static Logger logger = LoggerFactory.getLogger(FtpClient.class);


    public FtpClient(Credention credention, Settings settings) throws IOException {
        this.credention = credention;
        this.saveFolder = settings.getSaveFolder();
        this.ftp = new FTPClient();
        this.settings = (FtpSettings)settings;
        this.ftp.setConnectTimeout  (this.settings.getDataTimeOut());
        this.filePostfix = this.settings.getFilePostfix();

    }

    @Override
    public void connect() throws IOException {
        ftp.connect(credention.getServer(), credention.getPort());
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            logger.error("Exception in connecting to FTP Server: " + reply + " code.");

        }
        ftp.login(credention.getUser(), credention.getPassword());
        ftp.enterLocalPassiveMode();
        ftp.setFileType(settings.getFileType());
        this.workDirectory = ftp.printWorkingDirectory();
    }

    @Override
    public void downLoad() throws IOException {
        List<FTPFile> folders = getFilesFromRoot();

        for (FTPFile folder : folders) {
            if (folder.isDirectory()) {
                List<FTPFile> files = getFilesFromPath(workDirectory.concat("/").concat(folder.getName()));
                for (FTPFile file : files) {
                    downLoadFile(folder.getName(), file);
                }
            }
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

    @Override
    public List<FTPFile> getFilesFromRoot() {
        return getFilesFromPath(workDirectory);
    }


    public void downLoadFile(String proccessFolder, FTPFile pathFile) throws IOException {

        if (!pathFile.getName().endsWith(filePostfix))
            return;

        File localFolder = new File(saveFolder.concat(proccessFolder));
        File localFile = new File(saveFolder.concat(proccessFolder).concat("/").concat(pathFile.getName()));
        String remotePathFile = workDirectory.concat("/").concat(proccessFolder).concat("/").concat(pathFile.getName());
        logger.info("Process "+remotePathFile);

        if (!localFolder.exists())
            localFolder.mkdir();
        boolean exists = localFile.exists();
        long length = localFile.length();
        logger.info("Remote file size: "+length);
        if (exists && length == pathFile.getSize()) {
            logger.info("File is already exists , skipping.");
            return;
        } else if (exists) {
            logger.info("File is continued to downloading.");
            ftp.setRestartOffset(length);
        } else {

            if (!localFile.createNewFile())
                logger.error("Can't create file");

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
                logger.info("Downloading file completed.");
                ftp.deleteFile(remotePathFile);
                if (ftp.listFiles(workDirectory.concat(proccessFolder)).length == 0){
                    logger.info("Removing folder "+proccessFolder+" from "+workDirectory+".");
                    ftp.removeDirectory(workDirectory.concat(proccessFolder));
                }

            } else
                logger.error("File error downloading: after downloading size is not coinside.");
        } else {
            logger.error("File error downloading: compliting command wrong");
            outputStream.close();
            inputStream.close();
        }
    }
    @Override
    public void close() throws IOException {

        if (ftp.isConnected()) {
            ftp.logout();
            ftp.disconnect();
        }
    }


}
