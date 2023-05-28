package API;

import org.junit.jupiter.api.Test;

import java.io.IOException;


class SpotifyAPITest {
    @Test
    void testGetArtistImage() {
        String url = null;
        try {
            String token = SpotifyAPI.getToken();
            url = SpotifyAPI.getArtistImage("Paramore", token);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(url);
    }
}