package com.example.demo.clients;


import lombok.Data;

@Data
public  class Credention
{
    private  String server;
    private  int port;
    private  String user;
    private  String password;

    public Credention(String server, String user, String password) {
        this.server = server;
        this.user = user;
        this.password = password;
        this.port = 21;
    }
}

