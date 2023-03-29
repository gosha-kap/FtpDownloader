package com.example.demo.settings;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FtpSettings extends Settings {

    private int dataTimeOut = 0;
    private int fileType = org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;
    private String filePostfix;

    public FtpSettings(String filePostfix) {
        this.filePostfix = filePostfix;
    }
}
