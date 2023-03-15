package com.example.demo.clients.factory;

import com.example.demo.clients.FtpClient;
import com.example.demo.clients.MyClient;
import com.example.demo.clients.factory.ClientFacroty;
import com.example.demo.settings.Credention;
import com.example.demo.settings.Settings;

import java.io.IOException;

public class FtpClientFactory implements ClientFacroty {

    @Override
    public MyClient createClient(Credention credention, Settings setting) throws IOException {
        return new FtpClient(credention,setting);
    }
}
