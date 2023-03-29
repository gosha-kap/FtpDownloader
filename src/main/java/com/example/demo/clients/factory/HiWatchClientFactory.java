package com.example.demo.clients.factory;

import com.example.demo.clients.HiWatchClient;
import com.example.demo.clients.MyClient;
import com.example.demo.model.Credention;
import com.example.demo.settings.Settings;

public class HiWatchClientFactory implements ClientFacroty {

    @Override
    public MyClient createClient(Credention credention, Settings setting) {

        return new HiWatchClient(credention,setting);
    }
}
