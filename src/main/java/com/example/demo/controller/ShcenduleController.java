package com.example.demo.controller;

import com.example.demo.Jobs.DownloadJob;
import com.example.demo.model.ClientType;
import com.example.demo.clients.MyClient;
import com.example.demo.clients.factory.ClientFacroty;
import com.example.demo.dto.CheckConnection;
import com.example.demo.dto.JobList;
import com.example.demo.dto.JobDetailDTO;
import com.example.demo.model.ExSettings;
import com.example.demo.model.Credention;
import com.example.demo.model.Description;
import com.example.demo.service.JobRepeatService;
import com.example.demo.settings.*;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import java.io.IOException;
import java.time.*;
import java.util.*;

import static com.example.demo.Jobs.DownloadJob.createClientByType;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

@Slf4j
@Controller
public class ShcenduleController {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private JobRepeatService jobRepeatService;
    private final String REGULAR = "regular";
    private final String ONCE = "once";

    Logger logger = LoggerFactory.getLogger(ShcenduleController.class);

    @PostMapping(value = "/schendule", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String schenduleFtp(@Validated JobDetailDTO jobDetailDTO) throws SchedulerException {

        if (jobDetailDTO.getJobName() != null && jobDetailDTO.getJobGroup() != null) {
            JobKey jobKey = jobKey(jobDetailDTO.getJobName(), jobDetailDTO.getJobGroup());
            scheduler.deleteJob(jobKey);
            jobRepeatService.remove(jobKey.toString());
        }
        JobDetail jobDetail = buildJob(jobDetailDTO);
        // Add two triggers
        if (Objects.nonNull(jobDetailDTO.getRegular())) {
            scheduler.scheduleJob(jobDetail, buildTrigger(jobDetail, jobDetailDTO, REGULAR));
        } else
            scheduler.addJob(jobDetail, true);
        if (Objects.nonNull(jobDetailDTO.getOnce()))
            scheduler.scheduleJob(buildTrigger(jobDetail, jobDetailDTO, ONCE));
        return "redirect:/schendule";
    }

    @GetMapping("/schendule")
    public ModelAndView listOfJobs() throws SchedulerException {

        List<JobList> jobDTOList = new ArrayList<>();
        for (String group : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(groupEquals(group))) {
                JobDataMap dataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
                ClientType type = (ClientType) dataMap.get("type");
                Description description = (Description) dataMap.get("description");
                JobList jobDTO = new JobList(jobKey.getName(), jobKey.getGroup(), description.getAlias(), description.getAddress(), type.toString());
                String status = getJobStatus(jobKey);
                LocalDateTime nextFireTime = getTimers(jobKey).values().stream().sorted().findFirst().orElse(null);
                jobDTO.setLocalDateTime(nextFireTime);
                jobDTOList.add(jobDTO);
            }
        }
        ModelAndView mav = new ModelAndView("schendule");
        mav.addObject("jobs", jobDTOList);
        mav.addObject("jobDTO", new JobList());
        return mav;
    }

    private String getJobStatus(JobKey jobKey) throws SchedulerException {
        var status = scheduler.getTriggersOfJob(jobKey);
        if (status.isEmpty())
            return "NONE";
        return scheduler.getTriggerState(status.get(0).getKey()).toString();
    }

    @GetMapping("/addJob")
    public ModelAndView addJob() {
        ModelAndView modelAndView = new ModelAndView("addJob");
        JobDetailDTO jobDetailDTO = new JobDetailDTO();
        jobDetailDTO.setType("FTP");
        modelAndView.addObject("jobDetailDTO", jobDetailDTO);
        return modelAndView;
    }

    @PostMapping("/editJob")
    public ModelAndView editJob(@ModelAttribute JobList jobDTO) throws SchedulerException {

        JobKey jobKey = jobKey(jobDTO.getJobName(), jobDTO.getJobGroup());
        JobDataMap dataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
        Map<String, LocalDateTime> timers = getTimers(jobKey);
        JobDetailDTO jobDetailDTO = buildJobDto(dataMap, jobKey, timers);
        ModelAndView modelAndView = new ModelAndView("addJob");
        modelAndView.addObject("jobDetailDTO", jobDetailDTO);
        return modelAndView;
    }

    @PostMapping("/viewJob")
    public ModelAndView viewJob(@ModelAttribute JobList jobDTO) throws SchedulerException {
        JobKey jobKey = jobKey(jobDTO.getJobName(), jobDTO.getJobGroup());
        JobDataMap dataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
        Map<String, LocalDateTime> timers = getTimers(jobKey);
        JobDetailDTO jobDetail = buildJobDto(dataMap, jobKey, timers);
        ModelAndView modelAndView = new ModelAndView("viewJob");
        modelAndView.addObject("jobDatail", jobDetail);
        modelAndView.addObject("jobDTO", new JobList());
        return modelAndView;
    }

    @PostMapping(value = "/delJob")
    public String deleteJob(@ModelAttribute JobList jobDTO) throws SchedulerException {
        JobKey jobKey = jobKey(jobDTO.getJobName(), jobDTO.getJobGroup());
        scheduler.deleteJob(jobKey);
        CacheSettings.delete(jobKey.toString());
        jobRepeatService.remove(jobKey.toString());
        return "redirect:/schendule";
    }

    /* TODO*/
    /*make spring instead of shit code*/
    @PostMapping(value = "/check")
    public List<String> checkParametrs(@Valid @RequestBody CheckConnection loginForm, Errors errors) {
        ClientType type = ClientType.valueOf(loginForm.getType());
        int port = (Objects.isNull(loginForm.getPort())) ? type.getPort() : loginForm.getPort();
        int channel = (Objects.isNull(loginForm.getChannel())) ? 101 : loginForm.getChannel();
        ClientFacroty clientFacroty = createClientByType(ClientType.valueOf(loginForm.getType()));
        Credention credention = new Credention(loginForm.getIp(), port, loginForm.getLogin(), loginForm.getPassword());
        Settings settings = null;
        if (type.equals(ClientType.HiWatch)) {
            if (Objects.nonNull(loginForm.getFrom()) && Objects.nonNull(loginForm.getTo())) {
                settings = new HiWatchSettings(channel,loginForm.getFrom(),loginForm.getTo(),loginForm.isTimeShift());
            }
            else
                settings = new HiWatchSettings(channel,loginForm.isTimeShift());
        } else if (type.equals(ClientType.FTP)) {
                String filePostFix = Objects.isNull(loginForm.getFilePostfix()) ? "" : loginForm.getFilePostfix();
                settings = new FtpSettings(filePostFix);
        }

        try {
            MyClient client = clientFacroty.createClient(credention, settings);
            return   client.check();

        } catch (IOException e) {
            List<String> err = new ArrayList<>();
            err.add(e.getMessage());
            return err;
        }

    }

    private JobDetail buildJob(JobDetailDTO jobDetailDTO) throws SchedulerException {
        JobKey jobKey = new JobKey(jobDetailDTO.getAlias(), jobDetailDTO.getType());
        JobDataMap jobDataMap = buildJobDataMap(jobDetailDTO, jobKey);
        return JobBuilder.newJob(DownloadJob.class).withIdentity(jobKey)
                .withDescription(jobDetailDTO.getAlias())
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();

    }

    private JobDataMap buildJobDataMap(JobDetailDTO jobDetailDTO, JobKey jobKey) {
        JobDataMap dataMap = new JobDataMap();

        ////// Save credentials info ////
        Credention credention = new Credention(jobDetailDTO.getIp(), jobDetailDTO.getLogin(), jobDetailDTO.getPassword());
        ClientType type = ClientType.valueOf(jobDetailDTO.getType());
        if (Objects.isNull(jobDetailDTO.getPort())) {
            credention.setPort(type.getPort());
        } else {
            credention.setPort(jobDetailDTO.getPort());
        }
        dataMap.put("type", type);
        dataMap.put("credentials", credention);
        /////// Create settings by DTO values//////
        Settings settings;
        // Fill FTP Setting ///
        if (type.equals(ClientType.FTP)) {
            FtpSettings ftpSettings = new FtpSettings();
            ftpSettings.setDataTimeOut(Objects.nonNull(jobDetailDTO.getDataTimeOut()) ? jobDetailDTO.getDataTimeOut() : 0);
            ftpSettings.setFilePostfix(Objects.nonNull(jobDetailDTO.getFilePostfix()) ? jobDetailDTO.getFilePostfix() : "");
            settings = ftpSettings;
            // Fill HiWatch Setting ///
        } else if (type.equals(ClientType.HiWatch)) {
            HiWatchSettings hiWatchSettings = new HiWatchSettings();
            if (Objects.nonNull(jobDetailDTO.getChannel()))
                hiWatchSettings.setChannel(jobDetailDTO.getChannel());
            /* Once time run*/
            if (Objects.nonNull(jobDetailDTO.getOnce())) {
                if (Objects.nonNull(jobDetailDTO.getFrom()) && Objects.nonNull(jobDetailDTO.getTo())) {
                    LocalDateTime from = jobDetailDTO.isTimeShift() ? jobDetailDTO.getFrom().minusHours(10) : jobDetailDTO.getFrom();
                    LocalDateTime to = jobDetailDTO.isTimeShift() ? jobDetailDTO.getTo().minusHours(10) : jobDetailDTO.getTo();
                    hiWatchSettings.setFrom(from);
                    hiWatchSettings.setTo(to);
                } else {
                    /* Get last 24 hours */
                    LocalDateTime to = jobDetailDTO.isTimeShift() ? jobDetailDTO.getOnce().minusHours(10) : jobDetailDTO.getOnce();
                    LocalDateTime from = to.minusHours(24);
                    hiWatchSettings.setFrom(from);
                    hiWatchSettings.setTo(to);
                }
            }
            /* Regular time run*/
            else if (Objects.nonNull(jobDetailDTO.getRegular())) {
                /* in regular task takes last 24 hours period in executing job */
            }
            hiWatchSettings.setTimeShift(jobDetailDTO.isTimeShift());
            settings = hiWatchSettings;
        } else {
            throw new RuntimeException("Can't recognize type of download job.");
        }
        /*Fill common settings*/
        settings.setSaveFolder(jobDetailDTO.getSaveFolder());
        if (Objects.nonNull(jobDetailDTO.getNumOfTries()))
            settings.setNumOfTries(jobDetailDTO.getNumOfTries());
        else
            settings.setNumOfTries(1);
        //* Save repeat settings in db , because jobmap can be change between tries*//
        if (jobDetailDTO.isRepeatLater() && Objects.nonNull(jobDetailDTO.getNextTimeRun()) && Objects.nonNull(jobDetailDTO.getNumOfRepeats())) {
            ExSettings exSettings = new ExSettings(jobKey.toString(),
                    true, Long.valueOf(jobDetailDTO.getNextTimeRun()), jobDetailDTO.getNumOfRepeats());
            CacheSettings.save(jobRepeatService.save(exSettings));
        } else {
            if (CacheSettings.exist(jobKey.toString())) {
                CacheSettings.delete(jobKey.toString());
                jobRepeatService.remove(jobKey.toString());
            }
        }
        /*Telegram alarm chat*/
        if (Objects.nonNull(jobDetailDTO.getTelegramKey()) && Objects.nonNull(jobDetailDTO.getChatId())) {
            TelegramCredention telegramCredention = new TelegramCredention();
            telegramCredention.setTelegramKey(jobDetailDTO.getTelegramKey());
            telegramCredention.setChatId(jobDetailDTO.getChatId());
            settings.setTelegramCredention(telegramCredention);
        }

        dataMap.put("settings", settings);
        /*Decription job*/
        Description description = new Description();
        description.setAlias(jobDetailDTO.getAlias());
        description.setAddress(jobDetailDTO.getAddress());
        dataMap.put("description", description);
        return dataMap;
    }

    private JobDetailDTO buildJobDto(JobDataMap dataMap, JobKey jobKey, Map<String, LocalDateTime> timers) {
        LocalTime regular = Objects.isNull(timers.get(REGULAR)) ? null : timers.get(REGULAR).toLocalTime();
        LocalDateTime once = Objects.isNull(timers.get(ONCE)) ? null : timers.get(ONCE);

        JobDetailDTO jobDetail = new JobDetailDTO();
        jobDetail.setOnce(once);
        jobDetail.setRegular(regular);
        jobDetail.setJobGroup(jobKey.getGroup());
        jobDetail.setJobName(jobKey.getName());
        Credention credention = (Credention) dataMap.get("credentials");
        //Fill credentions part///
        if (Objects.nonNull(credention)) {
            jobDetail.setIp(credention.getServer());
            jobDetail.setPort(credention.getPort());
            jobDetail.setLogin(credention.getUser());
            jobDetail.setPassword(credention.getPassword());
        }
        Description description = (Description) dataMap.get("description");
        if (Objects.nonNull(description)) {
            jobDetail.setAlias(description.getAlias());
            jobDetail.setAddress(description.getAddress());
        }
        ClientType type = (ClientType) dataMap.get("type");
        jobDetail.setType(type.toString());
        Settings settings = (Settings) dataMap.get("settings");
        if (Objects.nonNull(settings)) {
            jobDetail.setSaveFolder(settings.getSaveFolder());
            jobDetail.setNumOfTries(settings.getNumOfTries());
            ExSettings exSettings = CacheSettings.exist(jobKey.toString()) ? CacheSettings.get(jobKey.toString()) : jobRepeatService.getByJobKey(jobKey.toString());
            if (Objects.nonNull(exSettings)) {
                jobDetail.setRepeatLater(exSettings.getRepeatLater());
                jobDetail.setNextTimeRun(exSettings.getNextTimeRun().intValue());
                jobDetail.setNumOfRepeats(exSettings.getNumOfRepeats());
            }

            if (type.equals(ClientType.FTP)) {
                FtpSettings ftpSettings = (FtpSettings) settings;
                jobDetail.setDataTimeOut(ftpSettings.getDataTimeOut());
                jobDetail.setFilePostfix(ftpSettings.getFilePostfix());
            }
            if (type.equals(ClientType.HiWatch)) {
                HiWatchSettings hiWatchSettings = (HiWatchSettings) settings;
                /* No need to check, default value is 101*/
                jobDetail.setChannel(hiWatchSettings.getChannel());
                jobDetail.setTimeShift(hiWatchSettings.isTimeShift());
                if (Objects.nonNull(hiWatchSettings.getFrom()) && Objects.nonNull(hiWatchSettings.getTo())) {
                    LocalDateTime from = hiWatchSettings.isTimeShift() ? hiWatchSettings.getFrom().plusHours(10) : hiWatchSettings.getFrom();
                    LocalDateTime to = hiWatchSettings.isTimeShift() ? hiWatchSettings.getTo().plusHours(10) : hiWatchSettings.getTo();
                    jobDetail.setFrom(from);
                    jobDetail.setTo(to);
                }
            }
            TelegramCredention telegramSettings = settings.getTelegramCredention();
            if (Objects.nonNull(telegramSettings)) {
                jobDetail.setTelegramKey(telegramSettings.getTelegramKey());
                jobDetail.setChatId(telegramSettings.getChatId());
            }
        }
        return jobDetail;
    }


    private Trigger buildTrigger(JobDetail jobDetail, JobDetailDTO jobDetailDTO, String type) {

        LocalDateTime onceDateTime = jobDetailDTO.getOnce();
        LocalTime regularTime = jobDetailDTO.getRegular();

        CronScheduleBuilder cronScheduleBuilder = null;
        if (type.equals(ONCE)) {
            cronScheduleBuilder = cronSchedule("0 " + onceDateTime.getMinute() + " " + onceDateTime.getHour() + " " + onceDateTime.getDayOfMonth() + " " + onceDateTime.getMonthValue() + " ? " + onceDateTime.getYear());
        } else if (type.equals(REGULAR)) {
            cronScheduleBuilder = cronSchedule("0 " + regularTime.getMinute() + " " + regularTime.getHour() + "  * * ?");
        }

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(UUID.randomUUID().toString(), "download-trigger")
                .withDescription(type)
                .withSchedule(cronScheduleBuilder)
                .build();
    }

    private Map<String, LocalDateTime> getTimers(JobKey jobKey) throws SchedulerException {
        Map<String, LocalDateTime> timers = new HashMap<>();
        List<Trigger> triggers = getAllTriggersKey(jobKey);
        triggers.forEach(x -> {
            if (x.getDescription().equals(REGULAR) && Objects.nonNull(x.getNextFireTime())) {
                timers.put(REGULAR, convertToLocalDateTime(x.getNextFireTime()));
            }
            if (x.getDescription().equals(ONCE) && Objects.nonNull(x.getNextFireTime())) {
                timers.put(ONCE, convertToLocalDateTime(x.getNextFireTime()));
            }
        });
        return timers;
    }

    private List<Trigger> getAllTriggersKey(JobKey jobKey) throws SchedulerException {
        return (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
    }

    private LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return LocalDateTime.ofInstant(
                dateToConvert.toInstant(), ZoneId.systemDefault());
    }
}
