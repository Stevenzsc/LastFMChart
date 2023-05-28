import API.SpotifyAPI;
import entity.Artist;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class LastFmExcelTest {


    @Test
    public void testGetTopArtists() throws Exception {
        String token = SpotifyAPI.getToken();
        List<List<Artist>> allDatesArtists = new ArrayList<>();

        System.out.println(allDatesArtists);
    }
}