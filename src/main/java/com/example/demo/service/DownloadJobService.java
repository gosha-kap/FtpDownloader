package com.example.demo.service;


import com.example.demo.dto.CheckResponse;
import com.example.demo.dto.CheckStatus;
import com.example.demo.dto.JobDetailDTO;
import com.example.demo.dto.JobList;
import com.example.demo.repository.*;
import com.example.demo.entity.*;
import com.mchange.v2.lang.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
@Slf4j
@Service
public class DownloadJobService {

    @Autowired
    DownloadJobJpa downloadJobJpa;
    @Autowired
    JobDetailJpa jobDetailJpa;
    @Autowired
    JobService jobService;
    @Autowired
    HIWatchJPA hiWatchJPA;
    @Autowired
    FtpJpa ftpJpa;
    @Autowired
    CredentialsJpa credentialsJpa;
    @Autowired
    DownloadSettingsJpa downloadSettingsJpa;
    @Autowired
    TelegramJpa telegramJpa;
    @Autowired
    FtpService ftpService;
    @Autowired
    HIWatchService hiWatchService;


    @Transactional
    public void save(JobDetailDTO jobDetailDTO){
        ////// check if update schedule job or new //////
        if(Objects.nonNull(jobDetailDTO.getJobKey()) && StringUtils.nonEmptyString(jobDetailDTO.getJobKey())){
            String jobkey = jobDetailDTO.getJobKey();
            if(jobService.checkIfRunning(jobkey)){
                log.error("Job is Running, can't change it.");
                throw new RuntimeException("You can change job During its work");
            }
            else{
                downloadJobJpa.deleteByJobKey(jobkey);
            }
        }

        DownloadJobEntity downloadJobEntity = new DownloadJobEntity();
        String key = UUID.randomUUID().toString();
        downloadJobEntity.setJobKey(key);
        jobDetailDTO.setJobKey(key);
        ////// JObDetailEntity /////
        JobDetailEntity jobDetailEntity = new JobDetailEntity( jobDetailDTO.getAlias(), jobDetailDTO.getNote(),ClientType.valueOf(jobDetailDTO.getType()));
        jobDetailEntity.setJobStatus(JobStatus.SCHEDULED);
        downloadJobEntity.setJobDetailEntity(jobDetailEntity);
        ////// Credentials  /////
        Credention credention = new Credention(jobDetailDTO.getIp(), jobDetailDTO.getLogin(), jobDetailDTO.getPassword());
        ClientType type = ClientType.valueOf(jobDetailDTO.getType());
        if (Objects.isNull(jobDetailDTO.getPort())) {
            credention.setPort(type.getPort());
        } else {
            credention.setPort(jobDetailDTO.getPort());
        }
        downloadJobEntity.setCredention(credention);
        //////  FTP Setting //////
        if (type.equals(ClientType.FTP)) {
            FtpSettings ftpSettings = new FtpSettings();
            ftpSettings.setDataTimeOut(jobDetailDTO.getDataTimeOut());
            ftpSettings.setFilePostfix(Objects.nonNull(jobDetailDTO.getFilePostfix()) ? jobDetailDTO.getFilePostfix() : "");
            downloadJobEntity.setFtpSettings(ftpSettings);
        //////  HiWatch Setting //////
        } else if (type.equals(ClientType.HiWatch)) {
            HiWatchSettings hiWatchSettings = new HiWatchSettings();
            if (Objects.nonNull(jobDetailDTO.getChannel()))
                hiWatchSettings.setChannel(jobDetailDTO.getChannel());
                hiWatchSettings.setTimeShift(jobDetailDTO.isTimeShift());
            /* Once time run*/
            if (Objects.nonNull(jobDetailDTO.getOnce())) {
                if (Objects.nonNull(jobDetailDTO.getFrom()) && Objects.nonNull(jobDetailDTO.getTo())) {
                    LocalDateTime from = jobDetailDTO.isTimeShift() ? jobDetailDTO.getFrom().minusHours(10) : jobDetailDTO.getFrom();
                    LocalDateTime to = jobDetailDTO.isTimeShift() ? jobDetailDTO.getTo().minusHours(10) : jobDetailDTO.getTo();
                    hiWatchSettings.setFromTime(from);
                    hiWatchSettings.setToTime(to);
                } else {
                    /* Get last 24 hours */
                    LocalDateTime to = jobDetailDTO.isTimeShift() ? jobDetailDTO.getOnce().minusHours(10) : jobDetailDTO.getOnce();
                    LocalDateTime from = to.minusHours(24);
                    hiWatchSettings.setFromTime(from);
                    hiWatchSettings.setToTime(to);
                }
            }
            /* Regular time run*/
            else if (Objects.nonNull(jobDetailDTO.getRegular())) {
                /* in regular task takes last 24 hours period then executing job */
            }
            downloadJobEntity.setHiWatchSettings(hiWatchSettings);
        } else {
            throw new IllegalArgumentException("Can't recognize type of download job.");
        }
        //////  Download Setting //////
        DownloadSettings downloadSettings = new DownloadSettings();
        downloadSettings.setSaveFolder(jobDetailDTO.getSaveFolder());
        downloadSettings.setNumOfTries(jobDetailDTO.getNumOfTries());
        downloadSettings.setRepeatLater(jobDetailDTO.isRepeatLater());
        downloadSettings.setNextTimeRun(jobDetailDTO.getNextTimeRun());
        downloadSettings.setNumOfRepeats(jobDetailDTO.getNumOfRepeats());
        downloadJobEntity.setDownloadSettings(downloadSettings);

        /*Telegram alarm chat*/
        if (Objects.nonNull(jobDetailDTO.getTelegramKey()) && Objects.nonNull(jobDetailDTO.getChatId())) {
            TelegramCredention telegramCredention = new TelegramCredention();
            telegramCredention.setTelegramKey(jobDetailDTO.getTelegramKey());
            telegramCredention.setChatId(jobDetailDTO.getChatId());
            downloadJobEntity.setTelegramCredention(telegramCredention);
        }
        downloadJobJpa.save(downloadJobEntity);
        jobService.save(jobDetailDTO);

    }
     public List<JobList> getJobs() {

          List<JobDetailEntity> entityList = jobDetailJpa.getAll();
          List<JobList> list = entityList.stream().map(entity->
              new JobList(entity.getDownloadJobEntity().getJobKey(),
                      entity.getAlias(), entity.getNote(),entity.getType().toString(),entity.getJobStatus().toString(),
                      jobService.getNextTimer(entity.getDownloadJobEntity().getJobKey()))).toList();

          return list;

    }

    public JobDetailDTO buildDto(String jobKey) {
        JobDetailDTO jobDetail = new JobDetailDTO();
        ////// Get Trigger times ///////
        Map<TimerType, LocalDateTime> timers = jobService.getTimers(jobKey);
        LocalTime regular = Objects.isNull(timers.get(TimerType.REGULAR)) ? null : timers.get(TimerType.REGULAR).toLocalTime();
        LocalDateTime once = Objects.isNull(timers.get(TimerType.ONCE)) ? null : timers.get(TimerType.ONCE);
        jobDetail.setOnce(once);
        jobDetail.setRegular(regular);
        jobDetail.setJobKey(jobKey.toString());

        DownloadJobEntity downloadJob = downloadJobJpa.getByJobId(jobKey);
        Credention credention = downloadJob.getCredention();
        //////Fill credentions part//////
        if (Objects.nonNull(credention)) {
            jobDetail.setIp(credention.getServer());
            jobDetail.setPort(credention.getPort());
            jobDetail.setLogin(credention.getUser());
            jobDetail.setPassword(credention.getPassword());
        }
        //////Fill description part//////
        JobDetailEntity jobDetailEntity = downloadJob.getJobDetailEntity();
        if (Objects.nonNull(jobDetailEntity)) {
            jobDetail.setAlias(jobDetailEntity.getAlias());
            jobDetail.setNote(jobDetailEntity.getNote());
            jobDetail.setType(jobDetailEntity.getType().toString());
        }
        //////Fill Settings part//////
        DownloadSettings downloadSettings = downloadJob.getDownloadSettings();
        if (Objects.nonNull(downloadSettings)) {
            jobDetail.setSaveFolder(downloadSettings.getSaveFolder());
            jobDetail.setNumOfTries(downloadSettings.getNumOfTries());
            jobDetail.setRepeatLater(downloadSettings.getRepeatLater());
            jobDetail.setNextTimeRun(downloadSettings.getNextTimeRun().intValue());
            jobDetail.setNumOfRepeats(downloadSettings.getNumOfRepeats());
            }
        //////Fill FTP part//////
            if (jobDetailEntity.getType().equals(ClientType.FTP)) {
                FtpSettings ftpSettings = downloadJob.getFtpSettings();
                jobDetail.setDataTimeOut(ftpSettings.getDataTimeOut());
                jobDetail.setFilePostfix(ftpSettings.getFilePostfix());

            }
        //////Fill HiWatch part//////
            if (jobDetailEntity.getType().equals(ClientType.HiWatch)) {
                HiWatchSettings hiWatchSettings = downloadJob.getHiWatchSettings();
                jobDetail.setChannel(hiWatchSettings.getChannel());
                jobDetail.setTimeShift(hiWatchSettings.isTimeShift());
                if (Objects.nonNull(hiWatchSettings.getFromTime()) && Objects.nonNull(hiWatchSettings.getToTime())) {
                    LocalDateTime from = hiWatchSettings.isTimeShift() ? hiWatchSettings.getFromTime().plusHours(10) : hiWatchSettings.getFromTime();
                    LocalDateTime to = hiWatchSettings.isTimeShift() ? hiWatchSettings.getToTime().plusHours(10) : hiWatchSettings.getToTime();
                    jobDetail.setFrom(from);
                    jobDetail.setTo(to);
                }
            }
        //////Fill telegram part//////
            TelegramCredention telegramSettings = downloadJob.getTelegramCredention();
            if (Objects.nonNull(telegramSettings)) {
                jobDetail.setTelegramKey(telegramSettings.getTelegramKey());
                jobDetail.setChatId(telegramSettings.getChatId());
            }

        return jobDetail;
    }
    @Transactional
    public void clearJob(String jobKey) {

        if(jobService.checkIfRunning(jobKey)){
            log.error("Job is Running, can't delete it.");
            throw  new RuntimeException("Job is Running, can't delete it.");
        }
        else{
            downloadJobJpa.deleteByJobKey(jobKey);
            jobService.deleteJob(jobKey);
        }
    }

    public DownloadJobEntity getByJobKey(String jobKey) {
        DownloadJobEntity downloadJobEntity = downloadJobJpa.getByJobId(jobKey);
        long id = downloadJobEntity.getId();
        downloadJobEntity.setJobDetailEntity(jobDetailJpa.findById(id).orElse(new JobDetailEntity()));
        downloadJobEntity.setFtpSettings(ftpJpa.findById(id).orElse(new FtpSettings()));
        downloadJobEntity.setHiWatchSettings(hiWatchJPA.findById(id).orElse(new HiWatchSettings()));
        downloadJobEntity.setDownloadSettings(downloadSettingsJpa.findById(id).orElse(new DownloadSettings()));
        downloadJobEntity.setCredention(credentialsJpa.findById(id).orElse(new Credention()));
        downloadJobEntity.setTelegramCredention(telegramJpa.findById(id).orElse(null));
        return downloadJobEntity;
    }

    public CheckResponse check(JobDetailDTO jobDetailDTO) {
        ////// Credentions ///////
        Credention credention = new Credention(jobDetailDTO.getIp(), jobDetailDTO.getLogin(), jobDetailDTO.getPassword());
        ClientType type = ClientType.valueOf(jobDetailDTO.getType());
        if (Objects.isNull(jobDetailDTO.getPort())) {
            credention.setPort(type.getPort());
        } else {
            credention.setPort(jobDetailDTO.getPort());
        }

        //////  FTP Setting //////
        if (type.equals(ClientType.FTP)) {
            FtpSettings ftpSettings = new FtpSettings();
            if(Objects.nonNull(jobDetailDTO.getDataTimeOut()))
                ftpSettings.setDataTimeOut(jobDetailDTO.getDataTimeOut());
            else
                ftpSettings.setDataTimeOut(0);
            ftpSettings.setFilePostfix(Objects.nonNull(jobDetailDTO.getFilePostfix()) ? jobDetailDTO.getFilePostfix() : "");
            return ftpService.check(credention,ftpSettings);
        }
        else if(type.equals(ClientType.HiWatch)) {
            HiWatchSettings hiWatchSettings = new HiWatchSettings();
            if (Objects.nonNull(jobDetailDTO.getChannel()))
                hiWatchSettings.setChannel(jobDetailDTO.getChannel());
                hiWatchSettings.setTimeShift(jobDetailDTO.isTimeShift());

            if (Objects.nonNull(jobDetailDTO.getFrom()) && Objects.nonNull(jobDetailDTO.getTo())) {
                    LocalDateTime from = jobDetailDTO.isTimeShift() ? jobDetailDTO.getFrom().minusHours(10) : jobDetailDTO.getFrom();
                    LocalDateTime to = jobDetailDTO.isTimeShift() ? jobDetailDTO.getTo().minusHours(10) : jobDetailDTO.getTo();
                    hiWatchSettings.setFromTime(from);
                    hiWatchSettings.setToTime(to);
                } else {
                    /* Get RECORD FOR  last 24 */
                    LocalDateTime to = jobDetailDTO.isTimeShift() ? LocalDateTime.now().minusHours(10) : LocalDateTime.now();
                    LocalDateTime from = to.minusHours(24);
                    hiWatchSettings.setFromTime(from);
                    hiWatchSettings.setToTime(to);
                }
            return hiWatchService.check(credention,hiWatchSettings);
        }
        CheckResponse checkResponse = new CheckResponse();
        checkResponse.setCheckStatus(CheckStatus.ERROR);
        checkResponse.setMessadge("Client type is unrecognized");
        return checkResponse;
    }
}
