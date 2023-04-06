package com.example.demo.clients;

import com.example.demo.entity.TelegramCredention;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Sender {


    public void sendTextMessage(TelegramCredention telegramSettings, String text) {
        TelegramBot telegramBot =  new TelegramBot(telegramSettings.getTelegramKey());
        SendMessage request = new SendMessage(telegramSettings.getChatId(), text)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false)
                .disableNotification(false);

        telegramBot.execute(request, new Callback<SendMessage, SendResponse>() {
            @Override
            public void onResponse(SendMessage request, SendResponse response) {
//                System.out.println(response);
            }
            @Override
            public void onFailure(SendMessage request, IOException e) {
//                e.printStackTrace();
            }
        });
    }
}




