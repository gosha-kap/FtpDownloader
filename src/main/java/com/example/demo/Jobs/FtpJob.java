package com.example.demo.Jobs;

import com.example.demo.Utils.Utils;
import com.example.demo.ftp.FtpClient;
import com.example.demo.ftp.Sender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@PersistJobDataAfterExecution
public class FtpJob extends QuartzJobBean {

    private static Logger logger = LoggerFactory.getLogger(FtpJob.class);
    private Messadge messadge = new Messadge();
    private final long NEXT_TRIGGER_EXECUTE = 5L; // next time executions if failed in minutes
    private  File rootDir;
    private  List<File> dirsInRoot;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String description = jobDataMap.getString("description");
        String ip = jobDataMap.getString("ip");
        String login = jobDataMap.getString("login");
        String pass = jobDataMap.getString("pass");
        String dir = jobDataMap.getString("path");
        String telegramKey = jobDataMap.getString("telegramKey");
        String chatId = jobDataMap.getString("chatId");

        FtpClient ftpClient = new FtpClient(ip, 21, login, pass);
        this.rootDir = new File(dir);
        this.dirsInRoot =  Arrays.asList(Objects.requireNonNull(rootDir.listFiles()));

        try {
            logger.info("Connecting...");
            ftpClient.open();
            FTPFile[] dirs = ftpClient.getFtpFiles(ftpClient.getDirectory());
            //Process folders
            for (FTPFile folder : dirs) {
                String folderName = folder.getName();
                logger.info("Process folder: " + folderName);
                //Create folder on storadge if not exist
                if (!Utils.isDirExist(folderName,dirsInRoot))
                    Utils.createDir(folderName,rootDir);
                //Process files in folder, download only files with defined postfix
                List<FTPFile>  files;
                //Map data with file size info to verify it later
                Map<String,Long> filesforDownload = new HashMap<>();
                try {
                    String path = ftpClient.getDirectory() + "/" + folder.getName();
                    files = Arrays.stream(ftpClient.getFtpFiles(path)).toList();
                    filesforDownload = Utils.getListFiles(files,"mp3");
                    Map<String, Long> finalFilesforDownload = filesforDownload;
                    files = files.stream().filter(x-> finalFilesforDownload.containsKey(x.getName())).collect(Collectors.toList());
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    messadge.setHasErrors(true);
                    messadge.addError(e.getMessage());
                    break;
                }

                ///////////////////////////
                ////// EXiT FROM LOOP HERE//
                ////////////////////////////
                if (filesforDownload.isEmpty()) {
                    logger.info("No defined files in folder");
                    ftpClient.close();
                    break;
                }
                ////////////////////////////
                ////////////////////////////

                for (FTPFile ftpFile : files) {

                    try {
                        logger.info("Start download: " + ftpFile.getName());
                        ftpClient.downloadFile(ftpFile, folder, rootDir.getAbsolutePath());
                        logger.info("Finish download: " + ftpFile.getName());

                    } catch (Exception e) {
                        ftpClient.close();
                        logger.error("Downloading error. Folder:" + folder.getName());
                        messadge.setHasErrors(true);
                        messadge.addError("Downloading error. Folder:" + folder.getName());
                        break;
                    }
                }

                Map<String, Long> downloadedFiles = Utils.getDownlodedFiles(rootDir.getAbsolutePath()+"/"+folderName);
                if (Utils.areEqual(downloadedFiles, filesforDownload)) {
                    try {
                        logger.info("Start removing downloaded files");
                        ftpClient.removeDirectory(folder.getName());
                    } catch (IOException e) {
                        logger.error("Removing dir error");
                    }
                }
            }
            logger.info("Good bye");
        } catch (Exception e) {
            logger.error(e.getMessage());
            messadge.setHasErrors(true);
            messadge.addError(e.getMessage());

        } finally {
            ftpClient.close();
            String errors = messadge.isHasErrors() ? messadge.outErrors() : null;
            if (Objects.nonNull(telegramKey) && Objects.nonNull(chatId)) {
                if (messadge.isHasErrors()) {
                    Utils.createTrigger(context,NEXT_TRIGGER_EXECUTE);
                    new Sender(telegramKey, chatId).sendTextMessage(description + ". Errors during downloading.\n" + errors);

                } else
                    new Sender(telegramKey, chatId).sendTextMessage(description + ": Ftp download completed.\n");
            }
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public class Messadge {
        private List<String> errors = new ArrayList<>();
        private boolean hasErrors = false;

        public void addError(String str) {
            this.errors.add(str);
        }


        public String outErrors() {
            StringBuilder stringBuilder = new StringBuilder();
            errors.forEach(str -> stringBuilder.append(str).append("\n"));
            return stringBuilder.toString();
        }
    }


}
