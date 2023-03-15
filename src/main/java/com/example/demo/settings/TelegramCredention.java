package com.example.demo.settings;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TelegramCredention implements Serializable {
    @NotNull
    private String telegramKey;
    @NotNull
    private String chatId;
}
