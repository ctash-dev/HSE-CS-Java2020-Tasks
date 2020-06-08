package ru.hse.cs.java2020.task03;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private static final List<ArrayList<String>> updates = new ArrayList<ArrayList<String>>();
    private static String token = null;
    private static String username = null;

//    public Bot(String token, String usrname) {
//        TOKEN = token;
//        USERNAME = usrname;
//
//        ApiContextInitializer.init();
//        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
//    }

    public static Bot newBuilder(String tkn, String usrname) {
        token = tkn;
        username = usrname;

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        Bot bot = new Bot();

        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            System.out.println("err");
        }
        return bot;
    }

    public List<ArrayList<String>> getUpdates() {
        List<ArrayList<String>> buffer = new ArrayList<ArrayList<String>>(updates);
        updates.clear();
        return buffer;
    }

    private synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            ArrayList<String> message = new ArrayList<>();
            message.add(update.getMessage().getChatId().toString());
            message.add(update.getMessage().getText());
            updates.add(message);
            sendMsg(update.getMessage().getChatId().toString(),
                    update.getMessage().getChatId().toString() + " your request is being proceeded");
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
