package API;

import Util.PropertyLoader;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static Constant.LFMConstant.SPOTIFY_API_TOKEN;

public class SpotifyAPI {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String SPOTIFY_API_URL = "https://api.spotify.com/v1/search";

    public static String getArtistImage(String artistName) throws IOException, JSONException {
        System.out.println("Calling Spotify API..." + artistName);
        PropertyLoader propertyLoader = new PropertyLoader("config.properties");

        HttpUrl url = HttpUrl.parse(SPOTIFY_API_URL).newBuilder()
                .addQueryParameter("q", artistName)
                .addQueryParameter("type", "artist")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + propertyLoader.getProperty(SPOTIFY_API_TOKEN))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JSONObject jsonResponse = new JSONObject(response.body().string());
            JSONObject artists = jsonResponse.optJSONObject("artists");
            if (artists == null) {
                throw new JSONException("JSONObject 'artists' not found in response");
            }

            JSONArray items = artists.optJSONArray("items");
            if (items == null) {
                throw new JSONException("JSONArray 'items' not found in response");
            }

            JSONObject artist = items.optJSONObject(0);
            if (artist == null) {
                throw new JSONException("JSONObject in 'items' at index 0 not found");
            }

            JSONArray images = artist.optJSONArray("images");
            if (images == null) {
                throw new JSONException("JSONArray 'images' not found in response");
            }

            JSONObject image = images.optJSONObject(0);
            if (image == null) {
                throw new JSONException("JSONObject in 'images' at index 0 not found");
            }

            return image.getString("url");
        }
    }
}