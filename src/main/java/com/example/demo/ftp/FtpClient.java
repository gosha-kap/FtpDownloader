package com.example.demo.ftp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class FtpClient {

    private final String server;
    private final int port;
    private final String user;
    private final String password;

    private String directory;
    private FTPClient ftp;

    private static Logger logger = LogManager.getLogger(FtpClient.class);


    public FtpClient(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public void open() throws IOException {

        ftp = new FTPClient();

        File log = new File("Y:/Logs/" + Thread.currentThread().getName() + "_ftp.log");
        if (!log.exists())
            log.createNewFile();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(new FileOutputStream(log))));
        ftp.setDataTimeout(15000);
        ftp.connect(server, port);

        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }
        ftp.login(user, password);
        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        this.setDirectory(ftp.printWorkingDirectory());
    }

    public Collection<String> listFiles(String path) throws IOException {
        FTPFile[] files = ftp.listFiles(path);
        return Arrays.stream(files)
                .map(FTPFile::getName)
                .collect(Collectors.toList());
    }

    public FTPFile[] getFtpFiles(String path) throws IOException {
        return ftp.listFiles(path);
    }

    public static void printFileDetails(FTPFile[] files) {
        long totalSize = 0;
        int count = 0;
        for (FTPFile file : files) {
            count++;
            totalSize += file.getSize();
        }
        double resultSize = totalSize / 1024 / 1024;
        String result = String.format("%.2f", resultSize);
        double averangeSize = resultSize / count;
        String result2 = String.format("%.2f", averangeSize);
        logger.info("Total " + count + " file(s)" + ", total size:" + result + "Mb, avarange size:" + result2 + "Mb.");
    }


    public void downloadFile(FTPFile file, FTPFile folder, String absolutePath) throws IOException {

        String path = "/" + folder.getName() + "/" + file.getName();
        String remoteFile = this.getDirectory() + path;
        File localFile = new File(absolutePath + path);

        boolean exists = localFile.exists();
        long length = localFile.length();
        if (exists && length == file.getSize()) {
            logger.info("File is exists.");
            return;
        } else if (exists) {
            ftp.setRestartOffset(length);
        } else {
            if (!localFile.createNewFile())
                throw new RuntimeException("Can't create file");
        }

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile, exists));
        InputStream inputStream = ftp.retrieveFileStream(remoteFile);


        byte[] bytesArray = new byte[4096];
        int bytesRead;


        while (ftp.isConnected() && (bytesRead = inputStream.read(bytesArray)) != -1) {
            outputStream.write(bytesArray, 0, bytesRead);
        }

        if (ftp.isConnected() && ftp.completePendingCommand()) {
            logger.info("File " + path + "has been downloaded successfully.");
        }

        outputStream.close();
        inputStream.close();
    }

    public void removeDirectory(String currentDir) throws IOException {
        String dirToList = directory + "/" + currentDir;
        FTPFile[] subFiles = ftp.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = dirToList + "/"
                        + currentFileName;
                boolean deleted = ftp.deleteFile(filePath);
                if (deleted) {
                    logger.info("DELETED the file: " + filePath);
                } else {
                    logger.error("CANNOT delete the file: "
                            + filePath);

                }
            }
        }

        // finally, remove the directory itself
        boolean removed = ftp.removeDirectory(dirToList);
        if (removed) {
            logger.info("REMOVED the directory: " + dirToList);
        } else {
            logger.error("CANNOT remove the directory: " + dirToList);

        }
    }


    public void close() {
        try {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }

        } catch (IOException ex) {
            logger.error("Error close ftp ftom ftp.close method. " + ex.getMessage());
        }
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}