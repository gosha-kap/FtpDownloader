package com.example.demo.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Description implements Serializable {
    private String alias;
    private String address;
}
