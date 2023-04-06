package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public  class Credention
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private  String server;
    private  int port;
    @Column(name = "login")
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

