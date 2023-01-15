package com.example.demo.ftp;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.io.IOException;


public class Sender {
    private  String chatId ;
    private TelegramBot telegramBot;
    public TelegramBot getTelegramBot() {
        return telegramBot;
    }

    public Sender(String botToken, String chatId){
        telegramBot = new TelegramBot(botToken);
        this.chatId = chatId;
    }
    public void sendTextMessage(  String text) {
        SendMessage request = new SendMessage(chatId, text)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false)
                .disableNotification(false);

        getTelegramBot().execute(request, new Callback<SendMessage, SendResponse>() {
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




