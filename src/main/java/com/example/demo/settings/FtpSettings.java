package com.example.demo.settings;


import lombok.Data;

@Data
public class FtpSettings extends Settings {

    private int dataTimeOut = 0;
    private int fileType = org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;
    private String filePostfix;

 }
