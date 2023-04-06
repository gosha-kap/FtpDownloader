//https://stackoverflow.com/questions/6390810/implement-a-simple-factory-pattern-with-spring-3-annotations
package com.example.demo.clients.factory;


import com.example.demo.clients.FtpClient;
import com.example.demo.clients.HiWatchClient;
import com.example.demo.clients.MyClient;
import com.example.demo.entity.ClientType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MyClientFactory {

    @Autowired
    HiWatchClient hiWatchClient;
    @Autowired
    FtpClient ftpClient;


        public   MyClient getService(ClientType type) {
            if(type.equals(ClientType.HiWatch))
                return hiWatchClient;
            else if(type.equals(ClientType.FTP))
                return ftpClient;
            else
                throw new RuntimeException("Unknown client type: " + type);

        }

}
