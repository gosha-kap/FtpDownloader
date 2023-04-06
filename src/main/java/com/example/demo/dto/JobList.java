package com.example.demo.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JobList {

    @NotNull
    private String jobKey;
     @NotNull
    private String alias;
    @NotNull
    private String note;
    @NotNull
    private String type;
    @NotNull
    private String status;

    private LocalDateTime localDateTime;


}
