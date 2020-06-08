package ru.hse.cs.java2020.task03;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class MainProcess {

    private final Bot tgbot;
    private final PostgrBD postgrdb;
    private final Client trClient;
    private final int authLen = 4;
    private final int getTaskLen = 2;
    private final int three = 3;
    boolean counterzero = false;

    public MainProcess(Bot telegrambot, PostgrBD db, Client client) {
        tgbot = telegrambot;
        postgrdb = db;
        trClient = client;
        counterzero = false;
    }

    public void updater(String chatId, String[] request) throws SQLException, TelegramApiException {
        switch (request[0]) {
            case "/authorize":
                authorize(chatId, request);
                break;
            case "/gettask":
                getTask(chatId, request);
                break;
            case "/createtask":
                createTask(chatId, request);
                break;
            case "/showqueues":
                showQueues(chatId);
                break;
            case "/showmytasks":
                counterzero = true;
                showMyTasks(chatId, request);
                break;
            case "/continue":
                counterzero = false;
                showMyTasks(chatId, request);
                break;
            default:
                showInfo(chatId);
                break;
        }
    }

    public void showInfo(String chatId) throws TelegramApiException {
        sendMessage(chatId,
                "Command list:\n"
                        + "Here you can recieve your personal token"
                        + "https://oauth.yandex.ru/authorize?response_type=token&client_id=b93d753ee4bf4a2caf8b4798025816ca \n"
                        + "/authorize TrackerToken X-Org-Id Username - to start using TrackerBot\n"
                        + "/createtask TaskName TaskDescription Queue [assignee] - to create a task with following parameters\n"
                        + "/gettask TaskId - to find task by Id and show main info\n"
                        + "/getQueues - to show all queues in Dashboard. Use it to create new tasks\n"
                        + "/getMyTasks [number] - to get tasks where you are an assignee \n Use new line as a separator \n");
    }

    public void authorize(String chatId, String[] request) throws SQLException, TelegramApiException {
        if (request.length < authLen) {
            showInfo(chatId);
        } else {
            postgrdb.insertData(chatId, new BDInfo(request[1], request[2], request[three]));
            sendMessage(chatId, "Authorization has been finished");
        }
    }

    private void sendMessage(String chatId, String text) throws TelegramApiException {
        tgbot.execute(new SendMessage(chatId, text));
    }

    public void getTask(String chatId, String[] request) throws TelegramApiException {
        var userInfo = postgrdb.getData(chatId);
        if (!userInfo.isPresent()) {
            sendMessage(chatId, "No Authorization has been provided");
            return;
        }
        if (request.length < getTaskLen) {
            showInfo(chatId);
        } else {
            try {
                var task = trClient.getTask(userInfo.get().getToken(), userInfo.get().getOrg(), request[1]);

                sendMessage(chatId, "task name: " + task.getName());
                sendMessage(chatId, "description: " + task.getDescription());
                sendMessage(chatId, "author: " + task.getAuthor());

                var assignedTo = task.getAssignedTo();
                assignedTo.ifPresent(s -> {
                    try {
                        sendMessage(chatId, "assignee: " + s);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                });

                var comments = task.getComments();
                if (comments.size() > 0) {
                    sendMessage(chatId, "comments:");
                    for (var comment : comments) {
                        sendMessage(chatId, comment.getAuthor() + " comment " + comment.getText());
                    }
                }

                var followers = task.getFollowers();
                if (followers.size() > 0) {
                    sendMessage(chatId, "followers:");
                    for (var follower : followers) {
                        sendMessage(chatId, follower);
                    }
                }

            } catch (IOException | InterruptedException exc) {
                sendMessage(chatId, "Undefined error occured");
            } catch (TrackerException exc) {
                sendMessage(chatId, "Authorization problem occurred");
                sendMessage(chatId, exc.getMessage());
            }
        }
    }

    public void createTask(String chatId, String[] request) throws TelegramApiException {

        var userInfo = postgrdb.getData(chatId);
        Optional<String> newTask;

        if (!userInfo.isPresent()) {
            sendMessage(chatId, "No Authorization has been provided");
            return;
        }

        if (request.length < authLen) {
            showInfo(chatId);
        } else {
            // no assignee
            if (request.length == authLen) {
                newTask = trClient.createTask(userInfo.get().getToken(), userInfo.get().getOrg(), request[1],
                        request[2], Optional.empty(), request[three]);
            } else {
                newTask = trClient.createTask(userInfo.get().getToken(), userInfo.get().getOrg(), request[1],
                        request[2], Optional.of(request[authLen]), request[three]);
            }
            if (newTask.isPresent()) {
                sendMessage(chatId, "Task has been created" + newTask.get());
            } else {
                sendMessage(chatId, "Undefined error occured");
            }
        }
    }

    public void showMyTasks(String chatId, String[] request) throws TelegramApiException, SQLException {

        if (counterzero) {
            postgrdb.updateCounter(chatId, 0);
        }

        int counterPage = postgrdb.getCounter(chatId);
        var userInfo = postgrdb.getData(chatId);

        if (!userInfo.isPresent()) {
            sendMessage(chatId, "No Authorization has been provided");
            return;
        }

        try {
            var tasks = trClient.getTasksByUser(userInfo.get().getToken(), userInfo.get().getOrg(),
                    userInfo.get().getUsername());
            int maxTasks = 1;
            if (request.length > 1) {
                maxTasks = Integer.parseInt(request[1]);
            }
            if (maxTasks >= tasks.size()) {
                for (int i = 0; i < tasks.size(); i++) {
                    sendMessage(chatId, tasks.get(i));
                }
            } else {
                int temper = counterPage;
                if (1 + temper > tasks.size()) {
                    sendMessage(chatId, "No more tasks!");
                    postgrdb.updateCounter(chatId, 0);
                    return;
                }
                for (int i = temper; i < (maxTasks + temper) && i < tasks.size(); i++) {
                    sendMessage(chatId, tasks.get(i));
                    counterPage++;
                }
                sendMessage(chatId, "Max tasks on page. Type /continue to proceed");
                postgrdb.updateCounter(chatId, counterPage);
                return;
            }
            sendMessage(chatId, "No more tasks!");
        } catch (TrackerException exc) {
            sendMessage(chatId, "Undefined error occured");
            System.err.println(exc.getMessage());
            sendMessage(chatId, exc.getMessage());
        }
    }

    public void showQueues(String chatId) throws TelegramApiException {

        try {
            var userInfo = postgrdb.getData(chatId);
            if (!userInfo.isPresent()) {
                sendMessage(chatId, "No Authorization has been provided");
                return;
            }
            var queues = trClient.getAllQueues(userInfo.get().getToken(), userInfo.get().getOrg());

            sendMessage(chatId, "Current queues:");
            for (var queue : queues) {
                sendMessage(chatId, "Name: " + queue.getKey() + " ID: " + queue.getId());
            }
        } catch (IOException | InterruptedException exc) {
            sendMessage(chatId, "Undefined error occured");
        }
    }
}
