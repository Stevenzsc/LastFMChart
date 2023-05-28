package API;

import Util.PropertyLoader;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import static Constant.LFMConstant.SPOTIFY_CLIENT_ID;
import static Constant.LFMConstant.SPOTIFY_CLIENT_SECRET;

public class SpotifyAPI {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String SPOTIFY_API_URL = "https://api.spotify.com/v1/search";

    public static String getArtistImage(String artistName, String token) throws IOException, JSONException {
        System.out.println("Calling Spotify API..." + artistName);
        PropertyLoader propertyLoader = new PropertyLoader("config.properties");

        HttpUrl url = HttpUrl.parse(SPOTIFY_API_URL).newBuilder()
                .addQueryParameter("q", artistName)
                .addQueryParameter("type", "artist")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
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

    public static String getToken() {
        PropertyLoader propertyLoader = new PropertyLoader("config.properties");
        String url = "https://accounts.spotify.com/api/token";
        String grantType = "client_credentials";
        String clientId = propertyLoader.getProperty(SPOTIFY_CLIENT_ID);
        String clientSecret = propertyLoader.getProperty(SPOTIFY_CLIENT_SECRET);

        try {
            // Prepare the POST data
            String postData = "grant_type=" + URLEncoder.encode(grantType, "UTF-8") +
                    "&client_id=" + URLEncoder.encode(clientId, "UTF-8") +
                    "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8");

            // Create OkHttpClient instance
            OkHttpClient client = new OkHttpClient();

            // Create the request body
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), postData);

            // Build the request
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            // Send the request and get the response
            Response response = client.newCall(request).execute();

            // Check if the request was successful
            if (response.isSuccessful()) {
                // Get the response body
                String responseBody = response.body().string();

                // Parse the response to extract the token
                String token = parseTokenFromResponse(responseBody);

                // Return the token
                return token;
            } else {
                System.out.println("Error: " + response.code() + " " + response.message());
            }

            // Close the response
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String parseTokenFromResponse(String responseBody) {
        try {
            // Parse the response body JSON to extract the token
            JSONObject responseJson = new JSONObject(responseBody);
            return responseJson.getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


}