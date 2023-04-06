package com.example.demo.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class CheckResponse {
    private CheckStatus checkStatus;
    private String messadge;
    private List<String> records;
}
