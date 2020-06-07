package ru.hse.cs.java2020.task03;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, SQLException, TelegramApiException {
        String BOT_TOKEN = "1246051746:AAHy8Vb559shtCvbKO1R8VrxhM_GsAy77XQ";
        //String TREKKER_TOKEN = "AgAAAAA0cFXJAAZjNdE1vkSYoUD9pX8LH_wk898";
        //String TREKKER_ID = "4144791";
        String BOT_USERNAME = "BetaTrackerbot";

        Client trackerClient = Client.Builder();
        Bot bot = Bot.Builder(BOT_TOKEN, BOT_USERNAME);
        PostgrBD base = new PostgrBD();
        MainProcess taskManager = new MainProcess(bot, base, trackerClient);

        while (true) {
            Thread.sleep(10000);
            List<ArrayList<String>> updates = bot.getUpdates();
            for (var update : updates) {
                taskManager.Update(update.get(0), update.get(1).split("\n"));
            }
        }
    }
}
