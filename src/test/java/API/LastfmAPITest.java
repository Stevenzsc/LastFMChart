package API;

import org.junit.jupiter.api.Test;

public class LastfmAPITest {

    @Test
    public void testGetArtistImageURL() throws Exception {
        String url = LastfmAPI.getArtistImageURL("Paramore");
        System.out.println(url);
    }
}