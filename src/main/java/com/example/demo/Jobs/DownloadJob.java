//https://stackoverflow.com/questions/6390810/implement-a-simple-factory-pattern-with-spring-3-annotations
package com.example.demo.Jobs;

import com.example.demo.clients.MyClient;
import com.example.demo.clients.Sender;
import com.example.demo.clients.factory.MyClientFactory;
import com.example.demo.entity.*;
import com.example.demo.repository.DownloadJobJpa;
import com.example.demo.repository.DownloadSettingsJpa;
import com.example.demo.repository.JobDetailJpa;
import com.example.demo.service.DownloadJobService;
import com.example.demo.service.JobService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@PersistJobDataAfterExecution
public class DownloadJob extends QuartzJobBean {

    @Autowired
    DownloadJobService downloadJobService;

    @Autowired
    JobDetailJpa jobDetailJpa;
    @Autowired
    DownloadSettingsJpa downloadSettingsJpa;
    @Autowired
    JobService jobService;
    @Autowired
    Sender sender;
    @Autowired
    MyClientFactory clientFactory;
    private Messadge messadge = new Messadge();

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        String jobKey = context.getJobDetail().getKey().getName();
        DownloadJobEntity downloadJob = null;
        try {
            downloadJob = downloadJobService.getByJobKey(jobKey);
        }catch (Exception e){
            jobService.deleteJob(jobKey);
            log.info("Deleting job with null data");
            throw new RuntimeException("Fake job with null data deleted");
        }

        DownloadSettings downloadSettings = downloadJob.getDownloadSettings();
        JobDetailEntity jobDetailEntity = downloadJob.getJobDetailEntity();
        int numberOfTriesToDownloads = downloadSettings.getNumOfTries();
        jobDetailEntity.setJobStatus(JobStatus.RUNNING);
        jobDetailJpa.save(jobDetailEntity);

        while (numberOfTriesToDownloads > 0) {
            try {
                log.info("Starting download job: " + jobDetailEntity.getAlias() + ".");
                MyClient client = clientFactory.getService(jobDetailEntity.getType());
                Object settings = null;
                if (jobDetailEntity.getType().equals(ClientType.FTP))
                    settings = downloadJob.getFtpSettings();
                else if (jobDetailEntity.getType().equals(ClientType.HiWatch))
                    settings = downloadJob.getHiWatchSettings();
                client.downLoad(downloadJob.getCredention(), downloadSettings.getSaveFolder(), settings);
            } catch (IOException e) {
                messadge.setError(e.getMessage());
                log.error(e.getMessage());
            } finally {
                numberOfTriesToDownloads--;
            }
        }
        if (messadge.isHasErrors() && downloadSettings.getRepeatLater()) {
            log.info("Next try will run in " + downloadSettings.getNextTimeRun() + " minute(s).");
            jobService.createNextTrigger(context.getJobDetail(), downloadSettings.getNextTimeRun());

            int numOfRepeat = downloadSettings.getNumOfRepeats();
            if (numOfRepeat > 0)
                numOfRepeat--;
            if (numOfRepeat == 0)
                downloadSettings.setRepeatLater(false);
            downloadSettings.setNumOfRepeats(numOfRepeat);
            downloadSettingsJpa.save(downloadSettings);
            jobDetailEntity.setJobStatus(JobStatus.ERROR);
        } else if (messadge.isHasErrors()) {
            log.info("Job is Done with errors");
            jobDetailEntity.setJobStatus(JobStatus.ERROR);

        } else {
            log.info("Job is Done");
            jobDetailEntity.setJobStatus(JobStatus.COMPLETED);
        }
        jobDetailJpa.save(jobDetailEntity);

        TelegramCredention telegramSettings = downloadJob.getTelegramCredention();
        if (Objects.nonNull(telegramSettings)) {
            String text = "";
            if (messadge.isHasErrors()) {
                text = jobDetailEntity.getAlias() + ". Errors during downloading.\n" + messadge.getError();
            } else
                text = jobDetailEntity.getAlias() + ". Completed " + jobDetailEntity.getType() + " job.\n";
            sender.sendTextMessage(telegramSettings, text);
        }
    }

    @Getter
    @NoArgsConstructor
    private class Messadge {
        private String error;
        private boolean hasErrors;

        public void setError(String lastError) {
            this.error = lastError;
            hasErrors = true;
        }
    }


}
