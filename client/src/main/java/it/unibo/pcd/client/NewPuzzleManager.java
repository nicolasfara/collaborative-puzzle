package it.unibo.pcd.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NewPuzzleManager {

    public JsonObject create(String urlImage, String rowImage, String colImage) throws IOException,
            InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        Gson gson = new Gson();
        JsonObject item = new JsonObject();
        item.addProperty("imageurl",urlImage);
        item.addProperty("rows",rowImage);
        item.addProperty("cols",colImage);

        var bodyJson = gson.toJson(item);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/create_puzzle"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        JsonElement element = gson.fromJson (response.body(), JsonElement.class);
        return element.getAsJsonObject();
    }
    public JsonObject join(String puzzleid) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        Gson gson = new Gson();
        JsonObject item = new JsonObject();
        item.addProperty("puzzleid",puzzleid);
        var bodyJson = gson.toJson(item);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/join_puzzle"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        JsonElement element = gson.fromJson (response.body(), JsonElement.class);
        return element.getAsJsonObject();

    }
}
