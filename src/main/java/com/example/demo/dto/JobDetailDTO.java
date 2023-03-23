package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobDetailDTO {

    @NotNull
    @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")
    private String ip;
    private Integer port;
    @NotNull
    private String login;
    @NotNull
    private String password;
    /////////////////////
    @NotNull
    private String alias;
    @NotNull
    private String address;
    /////////////////////
    @NotNull
    private String jobName;
    @NotNull
    private String jobGroup;
    //////Common Settings//
    @NotNull
    private String type;
    @NotNull
    private String saveFolder;
    private Integer numOfTries;
    private boolean repeatLater;
    private Integer nextTimeRun;
    private Integer numOfRepeats;
    ///////FTP/////////////
    private Integer dataTimeOut;
    private String filePostfix;
    ///////////////////////
    private Integer channel;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime from;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime to;
    private boolean timeShift;
    ///////////////////////
    private String telegramKey;
    private String chatId;
    ////////////////////////
    private LocalTime regular;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime once;

}
