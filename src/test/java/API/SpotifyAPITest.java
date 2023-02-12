package API;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyAPITest {
    @Test
    void testGetArtistImage() {
        String url = null;
        try {
            url = SpotifyAPI.getArtistImage("Paramore");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(url);
    }
}