package com.example.demo.model;


import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
public  class Credention implements Serializable
{
    private  String server;
    private  int port;
    private  String user;
    private  String password;

    public Credention(String server, String user, String password) {
        this.server = server;
        this.user = user;
        this.password = password;
        }

    public Credention(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
    }
}

