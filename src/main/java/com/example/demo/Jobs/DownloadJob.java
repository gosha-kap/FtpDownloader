package com.example.demo.Jobs;

import com.example.demo.Utils.Utils;
import com.example.demo.clients.*;
import com.example.demo.clients.factory.ClientFacroty;
import com.example.demo.clients.factory.FtpClientFactory;
import com.example.demo.clients.factory.HiWatchClientFactory;
import com.example.demo.entity.ExSettings;
import com.example.demo.model.Description;
import com.example.demo.service.JobRepeatService;
import com.example.demo.settings.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.IOException;
import java.util.*;

@Slf4j
@PersistJobDataAfterExecution
public class DownloadJob extends QuartzJobBean implements InterruptableJob {

    private static Logger logger = LoggerFactory.getLogger(DownloadJob.class);

    @Autowired
    private JobRepeatService jobRepeatService;
    private Messadge messadge = new Messadge();

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Credention credention = (Credention) jobDataMap.get("credentials");
        ClientType type =(ClientType) jobDataMap.get("type");
        Settings settings = (Settings) jobDataMap.get("settings");
        Description description = (Description) jobDataMap.get("description");

        String jobkey = context.getJobDetail().getKey().toString();
        ExSettings exSettings = CacheSettings.get(jobkey);
        if(Objects.isNull(exSettings))
            exSettings = jobRepeatService.getByJobKey(jobkey);
        if(Objects.isNull(exSettings))
            exSettings = new ExSettings(false,0L,0);

        while (settings.continueRepeat()) {
            try {
                logger.info("Starting download job: "+description.getAlias()+".");
                logger.info("Settings:num of tries download:"+settings.getNumOfTries()+"." );
                logger.info("If fails repeat after "+exSettings.getNextTimeRun()+" minute(s). Number of repeat: "+exSettings.getNumOfRepeats()+".");
                ClientFacroty clientFacroty = createClientByType(type);
                MyClient client = clientFacroty.createClient(credention, settings);
                client.connect();
                client.downLoad();
                client.close();
            } catch (IOException e) {
                messadge.setError(e.getMessage());
                logger.error(e.getMessage());
            }
            finally {
                settings.oneMoreTry();
            }
        }
        if (messadge.isHasErrors() && exSettings.getRepeatLater()) {
            logger.info("Next try will run in "+ exSettings.getNextTimeRun()+" minute(s).");
            Utils.createTrigger(context, exSettings.getNextTimeRun());
            exSettings.useOneRepeat();
        }
        else{
            logger.info("Job is stopped");
            CacheSettings.delete(jobkey);
        }

        TelegramCredention telegramSettings = settings.getTelegramCredention();
        if (Objects.nonNull(telegramSettings)) {
            if (messadge.isHasErrors()) {
                new Sender(telegramSettings).sendTextMessage(description.getAlias() + ". Errors during downloading.\n" + messadge.getError());
            } else
                new Sender(telegramSettings).sendTextMessage(description.getAlias() + ": Ftp download completed.\n");
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {

    }

    @Getter
    @NoArgsConstructor
    public class Messadge {
        private String error;
        private boolean hasErrors;
        public void setError(String lastError) {
            this.error = lastError;
            hasErrors = true;
        }
    }

     static ClientFacroty createClientByType(ClientType clientType){
        if(clientType.equals(ClientType.FTP)){
            return  new FtpClientFactory();
        }
        else if(clientType.equals(ClientType.HiWatch)){
            return  new HiWatchClientFactory();
        }
        else{
            throw new RuntimeException("Unknowned type client:"+clientType+".");
        }
    }
}
