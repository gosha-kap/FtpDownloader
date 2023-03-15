package com.example.demo.Utils;

import org.apache.commons.net.ftp.FTPFile;
import org.quartz.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;


import static org.quartz.CronScheduleBuilder.cronSchedule;

public class Utils {

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
        String out =("Total " + count + " file(s)" + ", total size:" + result + "Mb, avarange size:" + result2 + "Mb.");
    }


    public static Map<String, Long> getListFiles(List<FTPFile> files, String postFix) {
        Map<String, Long> downloadedFiles = new HashMap<>();
        for (FTPFile file : files) {
            if (file.getName().endsWith(postFix)) {
                downloadedFiles.put(file.getName(), file.getSize());
            }
        }
        return downloadedFiles;
    }

    public static boolean isDirExist(String path, List<File> dirs) {
        return dirs.stream().anyMatch(file -> file.getName().equals(path));
    }

    public static boolean createDir(String path, File rootDir) {
        File dir = new File(rootDir.getAbsolutePath() + "/" + path);
        return dir.mkdir();
    }
    public static boolean areEqual(Map<String, Long> first, Map<String, Long> second) {

        if (first.size() != second.size()) {
            return false;
        }
        return first.entrySet().stream()
                .allMatch(e -> e.getValue().equals(second.get(e.getKey())));
    }
    public static void createTrigger(JobExecutionContext context, long next_trigger_execute) {
        Scheduler scheduler = context.getScheduler();
        JobDetail jobDetail = context.getJobDetail();
        LocalDateTime localDateTime = LocalDateTime.now();
        Trigger trigger = buildTrigger(jobDetail, localDateTime.plusMinutes(next_trigger_execute));
        try {
            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public static org.quartz.Trigger buildTrigger(JobDetail jobDetail, LocalDateTime startAt) {

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(UUID.randomUUID().toString(), "ftp-trigger")
                .withDescription("once")
                .withSchedule(cronSchedule("0 " + startAt.getMinute() + " " + startAt.getHour() + " " + startAt.getDayOfMonth() + " " + startAt.getMonthValue() + " ? " + startAt.getYear()))
                .build();
    }

    public static Map<String, Long> getDownlodedFiles(String folderName) {

            File folder = new File(folderName);
            Map<String,Long> local = new HashMap<>();
            if(!folder.exists() || !folder.isDirectory()) return null;
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                try {
                    local.put(fileEntry.getName(), Files.size(fileEntry.toPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return local;

    }
}
