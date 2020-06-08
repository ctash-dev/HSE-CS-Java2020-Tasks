package ru.hse.cs.java2020.task03;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

public class Client {

    private static final int TIMEOUT = 30;
    private final HttpClient client;
    private final String queryBasis = "https://api.tracker.yandex.net/v2/";

    ArrayList<QueueHashMap> getAllQueues(String oauthToken, String orgID)
            throws java.io.IOException, java.lang.InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(queryBasis + "queues?"))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .headers("Authorization", "OAuth " + oauthToken,
                        "X-Org-Id", orgID)
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var body = response.body();

        ArrayList<QueueHashMap> result = new ArrayList<>();
        var queues = new JSONArray(body);

        for (var i = 0; i < queues.length(); i++) {
            var pair = queues.getJSONObject(i);
            result.add(new QueueHashMap(pair.getString("key"), pair.getInt("id")));
        }
        return result;
    }

    Optional<String> createTask(String oauthToken, String orgID, String name, String description,
                                Optional<String> user, String queueID) {
        // forms JSON request
        JSONObject JSON = new JSONObject();
        JSON.put("summary", name);
        JSON.put("description", description);
        JSON.put("queue", new JSONObject().put("id", queueID));
        if (user.isPresent()) {
            JSON.put("assignee", user.get());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(queryBasis + "issues?"))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .headers("Authorization", "OAuth " + oauthToken,
                        "X-Org-Id", orgID)
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toString()))
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                JSONObject obj = new JSONObject(response.body());
                return Optional.of(obj.getString("key"));
            } else {
                return Optional.empty();
            }
        } catch (IOException | InterruptedException exc) {
            return Optional.empty();
        }
    }

    ArrayList<String> getTasksByUser(String oauthToken, String orgID, String user)
            throws TrackerException {
        JSONObject request = new JSONObject();
        request.put("filter", new JSONObject().put("assignee", user));
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(queryBasis + "issues/_search?order=+updatedAt"))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .headers("Authorization", "OAuth " + oauthToken,
                        "X-Org-Id", orgID)
                .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                .build();
        try {
            var response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println(response.body());
                throw new TrackerException(response.body());
            }
            JSONArray responseJSON = new JSONArray(response.body());
            var result = new ArrayList<String>();
            for (int i = 0; i < responseJSON.length(); i++) {
                result.add(responseJSON.getJSONObject(i).getString("key"));
            }
            return result;
        } catch (IOException | InterruptedException exc) {
            System.err.println(exc.getMessage());
            throw new TrackerException(exc.getMessage());
        }
    }

    Task getTask(String oauthToken, String orgID, String task)
            throws java.io.IOException, java.lang.InterruptedException, TrackerException {

        HttpRequest MainRequest = HttpRequest.newBuilder()
                .uri(URI.create(queryBasis + "issues/" + task))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .headers("Authorization", "OAuth " + oauthToken,
                        "X-Org-Id", orgID)
                .GET()
                .build();

        var response = client.send(MainRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401 || response.statusCode() == 403 || response.statusCode() == 404 || response.statusCode() == 400) {
            throw new TrackerException(response.body());
        }

        var body = response.body();
        var res = new Task();

        JSONObject obj = new JSONObject(body);
        res.setName(obj.getString("key"));
        res.setDescription(obj.getString("summary"));
        if (obj.has("assignee")) {
            res.setAssignedTo(obj.getJSONObject("assignee").getString("display"));
        } else {
            res.setAssignedTo(null);
        }
        res.setAuthor(obj.getJSONObject("createdBy").getString("display"));
        if (obj.has("followers")) {
            var followers = obj.getJSONArray("followers");
            for (int i = 0; i < followers.length(); i++) {
                res.addFollower(followers.getJSONObject(i).getString("display"));
            }
        }

        HttpRequest CommentRequest = HttpRequest.newBuilder()
                .uri(URI.create(queryBasis + "issues/" + task + "/comments"))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .headers("Authorization", "OAuth " + oauthToken,
                        "X-Org-Id", orgID)
                .GET()
                .build();

        response = client.send(CommentRequest, HttpResponse.BodyHandlers.ofString());
        body = response.body();

        var comments = new JSONArray(body);
        for (int i = 0; i < comments.length(); i++) {
            var comment = comments.getJSONObject(i);
            res.addComment(new Comment(comment.getJSONObject("createdBy").getString("display"),
                    comment.getString("text")));
        }
        return res;
    }

    private Client() {
        client = HttpClient.newHttpClient();
    }

    public static Client Builder() {

        Client client = new Client();
        return client;
    }
}
