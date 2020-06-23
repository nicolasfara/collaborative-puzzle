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
            InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/create_puzzle?imageurl="+urlImage+"&rows="+rowImage+"&cols="+colImage))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        JsonElement element = gson.fromJson (response.body(), JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();
        return  jsonObj ;
    }
    public JsonObject join(String puzzleid) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/join_puzzle?puzzleid="+puzzleid))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        JsonElement element = gson.fromJson (response.body(), JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();
        return  jsonObj ;
    }
}
