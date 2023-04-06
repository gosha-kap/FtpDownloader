package com.example.demo.service;

import com.example.demo.clients.FtpClient;
import com.example.demo.dto.CheckResponse;
import com.example.demo.entity.Credention;
import com.example.demo.entity.FtpSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FtpService {
    @Autowired
    FtpClient ftpClient;

    CheckResponse check(Credention credention, FtpSettings ftpSettings){
        return ftpClient.check(credention,ftpSettings);
    }
}
