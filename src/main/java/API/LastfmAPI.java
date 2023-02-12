package API;

import Util.PropertyLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static Constant.LFMConstant.LASTFM_API_KEY;

public class LastfmAPI {

    public static String getArtistImageURL(String artist) throws Exception {
        PropertyLoader propertyLoader = new PropertyLoader("config.properties");
        String urlString = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=" +
                artist + "&api_key=" + propertyLoader.getProperty(LASTFM_API_KEY) + "&format=json";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject json = new JSONObject(response.toString());
        JSONObject artistJson = json.getJSONObject("artist");
        JSONArray imageArray = artistJson.getJSONArray("image");
        String imageURL = null;
        for (int i = 0; i < imageArray.length(); i++) {
            JSONObject image = imageArray.getJSONObject(i);
            if (image.getString("size").equals("extralarge")) {
                imageURL = image.getString("#text");
                break;
            }
        }
        return imageURL;
    }
}
