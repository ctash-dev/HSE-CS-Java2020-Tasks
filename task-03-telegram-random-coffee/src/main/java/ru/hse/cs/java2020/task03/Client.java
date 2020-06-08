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

public final class Client {

    private static final int TIMEOUT = 30;
    private final HttpClient client;
    private final String queryBasis = "https://api.tracker.yandex.net/v2/";
    private final int successAdded = 201;
    private final int success = 200;
    private final int forbidden = 403;
    private final int badRequest = 400;
    private final int notFound = 404;
    private final int unauthorizedError = 401;

    private Client() {
        client = HttpClient.newHttpClient();
    }

    public static Client newBuilder() {

        Client client = new Client();
        return client;
    }

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
        JSONObject json = new JSONObject();
        json.put("summary", name);
        json.put("description", description);
        json.put("queue", new JSONObject().put("id", queueID));
        if (user.isPresent()) {
            json.put("assignee", user.get());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(queryBasis + "issues?"))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .headers("Authorization", "OAuth " + oauthToken,
                        "X-Org-Id", orgID)
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == successAdded) {
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
            if (response.statusCode() != success) {
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

        HttpRequest mainRequest = HttpRequest.newBuilder()
                .uri(URI.create(queryBasis + "issues/" + task))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .headers("Authorization", "OAuth " + oauthToken,
                        "X-Org-Id", orgID)
                .GET()
                .build();

        var response = client.send(mainRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == unauthorizedError || response.statusCode() == forbidden
                || response.statusCode() == notFound || response.statusCode() == badRequest) {
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

        HttpRequest commentRequest = HttpRequest.newBuilder()
                .uri(URI.create(queryBasis + "issues/" + task + "/comments"))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .headers("Authorization", "OAuth " + oauthToken,
                        "X-Org-Id", orgID)
                .GET()
                .build();

        response = client.send(commentRequest, HttpResponse.BodyHandlers.ofString());
        body = response.body();

        var comments = new JSONArray(body);
        for (int i = 0; i < comments.length(); i++) {
            var comment = comments.getJSONObject(i);
            res.addComment(new Comment(comment.getJSONObject("createdBy").getString("display"),
                    comment.getString("text")));
        }
        return res;
    }
}
