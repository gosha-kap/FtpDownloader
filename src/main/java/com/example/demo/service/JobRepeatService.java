package com.example.demo.service;

import com.example.demo.entity.ExSettings;
import com.example.demo.entity.SettingsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class JobRepeatService {

    @Autowired
    private SettingsRepository settingsRepository;

    public ExSettings getByJobKey(String jobKey) {
        return settingsRepository.findByJobKey(jobKey);
    }

    public ExSettings save(ExSettings exSettings){
       return settingsRepository.save(exSettings);
    }

    @Transactional
    public void remove(String jobKey){
        settingsRepository.deleteByJobId(jobKey);
    }
}
