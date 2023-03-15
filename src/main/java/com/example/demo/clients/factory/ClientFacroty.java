package com.example.demo.clients.factory;

import com.example.demo.clients.MyClient;
import com.example.demo.settings.Credention;
import com.example.demo.settings.Settings;

import java.io.IOException;

public interface ClientFacroty {
    MyClient createClient(Credention credention, Settings setting) throws IOException;
}
