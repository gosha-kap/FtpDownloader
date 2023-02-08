package com.example.demo.clients;


import lombok.Data;

@Data
public class FtpSettings {

    private int dataTimeOut;
    private int fileType;
    private String filePostfix;
    private String saveFolder;

}
