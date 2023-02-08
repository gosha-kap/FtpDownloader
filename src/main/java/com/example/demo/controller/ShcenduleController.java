package com.example.demo.controller;

import com.example.demo.Jobs.FtpJob;
import com.example.demo.dto.JobDTO;
import com.example.demo.dto.JobDetailDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

@Slf4j
@Controller
public class ShcenduleController {

    @Autowired
    private Scheduler scheduler;
    private final String REGULAR = "regular";
    private final String ONCE = "once";

    @PostMapping(value = "/schendule", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String schenduleFtp(@Valid JobDetailDTO jobDetailDTO) throws SchedulerException {

        if (jobDetailDTO.getJobName() != null && jobDetailDTO.getJobGroup() != null) {
            JobKey jobKey = jobKey(jobDetailDTO.getJobName(), jobDetailDTO.getJobGroup());
            scheduler.deleteJob(jobKey);
        }
        JobDetail jobDetail = buildJob(jobDetailDTO);
        // Add two triggers
        if (Objects.nonNull(jobDetailDTO.getLocalTime()))
            scheduler.scheduleJob(jobDetail, buildTrigger(jobDetail, jobDetailDTO.getLocalTime()));
        else
            scheduler.addJob(jobDetail,true);
        if (Objects.nonNull(jobDetailDTO.getDateTime()))
            scheduler.scheduleJob(buildTrigger(jobDetail, jobDetailDTO.getDateTime()));
        return "redirect:/schendule";
    }


    @GetMapping("/schendule")
    public ModelAndView listOfJobs() throws SchedulerException {

        List<JobDTO> jobDTOList = new ArrayList<>();
        for (String group : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(groupEquals(group))) {
                JobDataMap dataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
                JobDTO jobDTO = new JobDTO(jobKey.getName(), jobKey.getGroup(), dataMap.getString("description"));
                LocalDateTime nextFireTime  = getTimers(jobKey).values().stream().sorted().findFirst().orElse(null);
                jobDTO.setLocalDateTime(nextFireTime);
                jobDTOList.add(jobDTO);
            }
        }
        ModelAndView mav = new ModelAndView("schendule");
        mav.addObject("jobs", jobDTOList);
        mav.addObject("jobDTO", new JobDTO());
        return mav;
    }

    @GetMapping("/addJob")
    public ModelAndView addJob() {
        ModelAndView modelAndView = new ModelAndView("addJob");
        JobDetailDTO jobDetailDTO = new JobDetailDTO();
        modelAndView.addObject("jobDetailDTO", jobDetailDTO);
        return modelAndView;
    }

    @PostMapping("/editJob")
    public ModelAndView editJob(@ModelAttribute JobDTO jobDTO) throws SchedulerException {
        ModelAndView modelAndView = new ModelAndView("addJob");
        JobKey jobKey = jobKey(jobDTO.getJobName(), jobDTO.getJobGroup());
        JobDetailDTO jobDetailDTO = getJobDetalDTOByKey(jobKey, jobDTO);
        modelAndView.addObject("jobDetailDTO", jobDetailDTO);
        return modelAndView;
    }

    @PostMapping("/viewJob")
    public ModelAndView viewJob(@ModelAttribute JobDTO jobDTO) throws SchedulerException {
        JobKey jobKey = jobKey(jobDTO.getJobName(), jobDTO.getJobGroup());
        JobDataMap dataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
        Map<String, LocalDateTime> timers = getTimers(jobKey);
        //!!!!///
        LocalTime regularTimer = Objects.isNull(timers.get(REGULAR)) ? null : timers.get(REGULAR).toLocalTime();
        LocalDateTime onceTimer =  Objects.isNull(timers.get(ONCE)) ? null : timers.get(ONCE);

        JobDetailDTO jobDetail = new JobDetailDTO(dataMap.getString("description"),
                dataMap.getString("ip"),
                dataMap.getString("login"),
                dataMap.getString("pass"),
                jobDTO.getJobName(),
                jobDTO.getJobGroup(),
                dataMap.getString("path"),
                dataMap.getString("telegramKey"),
                dataMap.getString("chatId"),
                regularTimer,
                onceTimer
        );
        ModelAndView modelAndView = new ModelAndView("viewJob");
        modelAndView.addObject("jobDatail", jobDetail);
        modelAndView.addObject("jobDTO", new JobDTO());
        return modelAndView;
    }


    @PostMapping(value = "/delJob")
    public String deleteJob(@ModelAttribute JobDTO jobDTO) throws SchedulerException {
        scheduler.deleteJob(jobKey(jobDTO.getJobName(), jobDTO.getJobGroup()));
        return "redirect:/schendule";

    }

    private JobDetail buildJob(JobDetailDTO jobDetailDTO) throws SchedulerException {
        JobDataMap jobDataMap = buildJobDataMap(jobDetailDTO);
        return JobBuilder.newJob(FtpJob.class).withIdentity(UUID.randomUUID().toString(), "ftp-download")
                .withDescription(jobDetailDTO.getDescription())
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();

    }

    private JobDataMap buildJobDataMap(JobDetailDTO jobDetailDTO) {
        JobDataMap dataMap = new JobDataMap();
               dataMap.put("ip", jobDetailDTO.getIp());
        dataMap.put("login", jobDetailDTO.getLogin());
        dataMap.put("pass", jobDetailDTO.getPassword());
        dataMap.put("description", jobDetailDTO.getDescription());
        dataMap.put("path", jobDetailDTO.getPath());
        dataMap.put("telegramKey", jobDetailDTO.getTelegramKey());
        dataMap.put("chatId", jobDetailDTO.getChatId());
        return dataMap;
    }

    private JobDetailDTO getJobDetalDTOByKey(JobKey jobKey, JobDTO jobDTO) throws SchedulerException {

        Map<String, LocalDateTime> timers = getTimers(jobKey);
        LocalTime regularTimer = Objects.isNull(timers.get(REGULAR)) ? null : timers.get(REGULAR).toLocalTime();
        LocalDateTime onceTimer =  Objects.isNull(timers.get(ONCE)) ? null : timers.get(ONCE);

        JobDataMap dataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
        JobDetailDTO jobDetailDTO = new JobDetailDTO();
        jobDetailDTO.setDescription(dataMap.getString("description"));
        jobDetailDTO.setIp(dataMap.getString("ip"));
        jobDetailDTO.setLogin(dataMap.getString("login"));
        jobDetailDTO.setPassword(dataMap.getString("pass"));
        jobDetailDTO.setPath(dataMap.getString("path"));
        jobDetailDTO.setLocalTime(regularTimer);
        jobDetailDTO.setDateTime(onceTimer);
        jobDetailDTO.setJobName(jobDTO.getJobName());
        jobDetailDTO.setJobGroup(jobDTO.getJobGroup());
        jobDetailDTO.setTelegramKey(dataMap.getString("telegramKey"));
        jobDetailDTO.setChatId(dataMap.getString("chatId"));
        return jobDetailDTO;
    }

    private org.quartz.Trigger buildTrigger(JobDetail jobDetail, LocalTime startAt) {

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(UUID.randomUUID().toString(), "ftp-trigger")
                .withDescription(REGULAR)
                .withSchedule(cronSchedule("0 " + startAt.getMinute() + " " + startAt.getHour() + "  * * ?"))
                .build();
    }

    private org.quartz.Trigger buildTrigger(JobDetail jobDetail, LocalDateTime startAt) {

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(UUID.randomUUID().toString(), "ftp-trigger")
                .withDescription(ONCE)
                .withSchedule(cronSchedule("0 " + startAt.getMinute() + " " + startAt.getHour() + " " + startAt.getDayOfMonth() + " " + startAt.getMonthValue() + " ? " + startAt.getYear()))
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
