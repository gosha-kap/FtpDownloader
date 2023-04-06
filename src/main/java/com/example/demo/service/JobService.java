package com.example.demo.service;

import com.example.demo.Jobs.DownloadJob;
import com.example.demo.dto.JobDetailDTO;
import com.example.demo.entity.TimerType;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static org.quartz.CronScheduleBuilder.cronSchedule;

@Service
@Slf4j
public class JobService {
    @Autowired
    private Scheduler scheduler;

    public void save(JobDetailDTO jobDetailDTO) {
        JobDetail jobDetail = null;
        try {
            jobDetail = buildJobDetail(jobDetailDTO);
        } catch (SchedulerException e) {
            log.error("Error in creating job detail,msg: " + e.getMessage());
            throw new RuntimeException(e);
        }
        Trigger trigger = buildTrigger(jobDetail, jobDetailDTO);
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Error schendule job,msg: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private JobDetail buildJobDetail(JobDetailDTO jobDetailDTO) throws SchedulerException {
        JobKey jobKey = new JobKey(jobDetailDTO.getJobKey());
        return JobBuilder.newJob(DownloadJob.class).withIdentity(jobKey)
                .withDescription(jobDetailDTO.getAlias())
                .storeDurably()
                .build();

    }

    private Trigger buildTrigger(JobDetail jobDetail, JobDetailDTO jobDetailDTO) {

        LocalDateTime onceDateTime = jobDetailDTO.getOnce();
        LocalTime regularTime = jobDetailDTO.getRegular();
        CronScheduleBuilder cronScheduleBuilder = null;
        TimerType timerType = null;

        if (Objects.nonNull(onceDateTime)) {
            cronScheduleBuilder = cronSchedule("0 " + onceDateTime.getMinute() + " " + onceDateTime.getHour() + " " + onceDateTime.getDayOfMonth() + " " + onceDateTime.getMonthValue() + " ? " + onceDateTime.getYear());
            timerType = TimerType.ONCE;
        } else if (Objects.nonNull(regularTime)) {
            cronScheduleBuilder = cronSchedule("0 " + regularTime.getMinute() + " " + regularTime.getHour() + "  * * ?");
            timerType = TimerType.REGULAR;
        }
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(UUID.randomUUID().toString(), "download-trigger")
                .withDescription(timerType.toString())
                .withSchedule(cronScheduleBuilder)
                .build();
    }

    public LocalDateTime getNextTimer(String jobKey) {
        List<Trigger> triggers = getAllTriggersKey(JobKey.jobKey(jobKey));
        return triggers.stream().map(x ->
                convertToLocalDateTime(x.getNextFireTime())).toList().stream().sorted().findFirst().orElse(null);
    }

    private LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return LocalDateTime.ofInstant(
                dateToConvert.toInstant(), ZoneId.systemDefault());
    }

    public Map<TimerType, LocalDateTime> getTimers(String jobKey) {
        Map<TimerType, LocalDateTime> timers = new HashMap<>();
        List<Trigger> triggers = getAllTriggersKey(JobKey.jobKey(jobKey));
        triggers.forEach(x -> {
            if (x.getDescription().equals(TimerType.REGULAR.toString()) && Objects.nonNull(x.getNextFireTime())) {
                timers.put(TimerType.REGULAR, convertToLocalDateTime(x.getNextFireTime()));
            }
            if (x.getDescription().equals(TimerType.ONCE.toString()) && Objects.nonNull(x.getNextFireTime())) {
                timers.put(TimerType.ONCE, convertToLocalDateTime(x.getNextFireTime()));
            }
        });
        return timers;
    }

    private List<Trigger> getAllTriggersKey(JobKey jobKey) {
        List<Trigger> triggers = null;
        try {
            triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
        } catch (SchedulerException e) {
            log.error("No triggers find for job: " + jobKey);
        }
        return triggers;
    }


    public boolean checkIfRunning(String jobkey) {
        List<JobExecutionContext> jobs = null;
        try {
            jobs = scheduler.getCurrentlyExecutingJobs();
        } catch (SchedulerException e) {
            log.error("Can't get schedule context: check if run");
            throw new RuntimeException("Can't get schedule context: check if run " + e);
        }
        return Objects.nonNull(jobs.stream().filter(x -> x.getJobDetail().getKey().equals(JobKey.jobKey(jobkey))).findAny().orElse(null));
    }


    public void deleteJob(String jobKey) {
        try {
            scheduler.deleteJob(JobKey.jobKey(jobKey));
        } catch (SchedulerException e) {
            log.error("Can't get schedule context: delete");
            throw new RuntimeException("Can't get schedule context: delete. "+ e);
        }
    }



    public void createNextTrigger(JobDetail jobDetail, Long nextTimeRun)  {
        JobDetailDTO jobDetailDTO = new JobDetailDTO();
        jobDetailDTO.setOnce(LocalDateTime.now().plusMinutes(nextTimeRun));
        Trigger trigger = buildTrigger(jobDetail,jobDetailDTO);
        try {
            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            log.error("Can't schedule  failed job"+e.getMessage());
            throw new RuntimeException("Can't schedule  failed job");
        }
    }
}
