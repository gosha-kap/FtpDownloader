package com.example.demo.clients;


import com.example.demo.settings.Credention;
import com.example.demo.settings.FtpSettings;
import com.example.demo.settings.Settings;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

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
            throw new IOException("Exception in connecting to FTP Server: " + reply + " code.");
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

        if (!localFolder.exists())
            localFolder.mkdir();
        boolean exists = localFile.exists();
        long length = localFile.length();
        if (exists && length == pathFile.getSize()) {
            return;
        } else if (exists) {
            ftp.setRestartOffset(length);
        } else {

            if (!localFile.createNewFile())
                throw new RuntimeException("Can't create file");
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
                ftp.deleteFile(remotePathFile);
                if (ftp.listFiles(workDirectory.concat(proccessFolder)).length == 0)
                    ftp.removeDirectory(workDirectory.concat(proccessFolder));
            } else
                throw new RuntimeException("File error downloading: size wrong.");
        } else {
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
