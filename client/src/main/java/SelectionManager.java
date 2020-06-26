import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;

public class SelectionManager {

	private boolean selectionActive = false;
	private Tile selectedTile;


	public void selectTile(final Tile tile, final String puzzleID,
						   final String playerID,
						   final Listener listener) throws IOException, InterruptedException {
		
		if(selectionActive) {
			selectionActive = false;
			Gson gson = new Gson();
			JsonObject item = new JsonObject();
			item.addProperty("puzzleid", puzzleID);
			item.addProperty("playerid",playerID);
			item.addProperty("source", Integer.toString(selectedTile.getCurrentPosition()));
			item.addProperty("destination", Integer.toString(tile.getCurrentPosition()));

			var bodyJson = gson.toJson(item);
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:8080/api/swap"))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(bodyJson))
					.build();
			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString());

			System.out.println(response.body());
			swap(selectedTile, tile);
			listener.onSwapPerformed();
		} else {
			selectionActive = true;
			selectedTile = tile;
		}
	}

	private void swap(final Tile t1, final Tile t2) {
		int pos = t1.getCurrentPosition();
		t1.setCurrentPosition(t2.getCurrentPosition());
		t2.setCurrentPosition(pos);
	}
	
	@FunctionalInterface
	interface Listener{
		void onSwapPerformed();
	}
}
