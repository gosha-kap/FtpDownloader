package com.example.demo.clients;

import com.example.demo.settings.FtpSettings;

import java.io.Serializable;

public enum ClientType implements Serializable {
    FTP(21),
    HiWatch(80);

    private int port;

    ClientType(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
