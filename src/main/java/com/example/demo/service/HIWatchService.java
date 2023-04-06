package com.example.demo.service;

import com.example.demo.clients.HiWatchClient;
import com.example.demo.dto.CheckResponse;
import com.example.demo.entity.Credention;
import com.example.demo.entity.FtpSettings;
import com.example.demo.entity.HiWatchSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HIWatchService {
    @Autowired
    HiWatchClient hiWatchClient;

    CheckResponse check(Credention credention, HiWatchSettings hiWatchSettings){
        return hiWatchClient.check(credention,hiWatchSettings);
    }

}
