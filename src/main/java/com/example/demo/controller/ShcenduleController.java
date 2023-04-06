package com.example.demo.controller;
import com.example.demo.dto.CheckResponse;
import com.example.demo.dto.CheckStatus;
import com.example.demo.dto.JobList;
import com.example.demo.dto.JobDetailDTO;
import com.example.demo.service.DownloadJobService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.util.*;
import static org.quartz.JobKey.jobKey;

@Slf4j
@Controller
public class ShcenduleController {

    @Autowired
    private DownloadJobService descriptionService;

    @PostMapping(value = "/jobs", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String schenduleFtp(@Validated JobDetailDTO jobDetailDTO)  {
        descriptionService.save(jobDetailDTO);
        return "redirect:/jobs";
    }

    @GetMapping("/jobs")
    public ModelAndView listOfJobs()  {
        List<JobList> jobDTOList = descriptionService.getJobs();
        ModelAndView mav = new ModelAndView("schendule");
        mav.addObject("jobs", jobDTOList);
        mav.addObject("jobDTO", new JobList());
        return mav;
    }

    @GetMapping("/addJob")
    public ModelAndView addJob() {
        ModelAndView modelAndView = new ModelAndView("addJob");
        JobDetailDTO jobDetailDTO = new JobDetailDTO();
        jobDetailDTO.setType("FTP"); // to avoid null in view template
        modelAndView.addObject("jobDetailDTO", jobDetailDTO);
        return modelAndView;
    }

    @PostMapping("/editJob")
    public ModelAndView editJob(@ModelAttribute JobList jobDTO)  {

        JobDetailDTO jobDetailDTO = descriptionService.buildDto(jobDTO.getJobKey());
        ModelAndView modelAndView = new ModelAndView("addJob");
        modelAndView.addObject("jobDetailDTO", jobDetailDTO);
        return modelAndView;
    }

    @PostMapping("/viewJob")
    public ModelAndView viewJob(@ModelAttribute JobList jobDTO)  {

        JobDetailDTO jobDetail = descriptionService.buildDto(jobDTO.getJobKey());
        ModelAndView modelAndView = new ModelAndView("viewJob");
        modelAndView.addObject("jobDatail", jobDetail);
        modelAndView.addObject("jobDTO", new JobList());
        return modelAndView;
    }
    @PostMapping(value = "/delJob")
    public String deleteJob(@ModelAttribute JobList jobDTO)  {
        descriptionService.clearJob(jobDTO.getJobKey());
        return "redirect:/jobs";
    }
    @PostMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> checkParametrs(@RequestBody  JobDetailDTO jobDetailDTO) {

         CheckResponse response = descriptionService.check(jobDetailDTO);
         return   ResponseEntity.ok(response);

    }














}
