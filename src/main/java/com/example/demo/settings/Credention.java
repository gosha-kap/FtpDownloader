package com.example.demo.settings;


import lombok.Data;

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
}

