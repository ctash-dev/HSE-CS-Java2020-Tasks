package ru.hse.cs.java2020.task03;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, SQLException, TelegramApiException {
        final String botToken = "1246051746:AAHy8Vb559shtCvbKO1R8VrxhM_GsAy77XQ";
        //String TREKKER_TOKEN = "AgAAAAA0cFXJAAZjNdE1vkSYoUD9pX8LH_wk898";
        //String TREKKER_ID = "4144791";
        final String botUsername = "BetaTrackerbot";
        final int mstimer = 10000;

        Client trackerClient = Client.newBuilder();
        Bot bot = Bot.newBuilder(botToken, botUsername);
        PostgrBD base = new PostgrBD();
        MainProcess taskManager = new MainProcess(bot, base, trackerClient);

        while (true) {
            Thread.sleep(mstimer);
            List<ArrayList<String>> updates = bot.getUpdates();
            for (var update : updates) {
                taskManager.updater(update.get(0), update.get(1).split("\n"));
            }
        }
    }
}
