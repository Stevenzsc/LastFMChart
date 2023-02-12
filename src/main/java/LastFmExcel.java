import java.io.*;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import API.SpotifyAPI;
import Util.PropertyLoader;
import entity.Artist;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import static Constant.LFMConstant.*;

public class LastFmExcel {

    private static List<Artist> artists = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        List<List<Long>> dates = getWeeklyDates();
        List<List<Artist>> artistsByWeek = getTopArtists(dates);
        writeExcelFile(artistsByWeek, dates);
    }

    private static Map<String, String> artistImageCache = new HashMap<>();
    private static final String CACHE_FILE = "artistImageCache.ser";

    private static List<List<Artist>> getTopArtists(List<List<Long>> dates) throws Exception {
        List<List<Artist>> allDatesArtists = new ArrayList<>();
        loadCache();
        for (int i = 0; i < dates.size(); i++) {
            List<Artist> artistsTemp = new ArrayList<>();
            System.out.printf("Start to process %s out of %s%n", i + 1, dates.size());
            String fromDate = String.valueOf(dates.get(i).get(0));
            String toDate = String.valueOf(dates.get(i).get(1));

            System.out.printf("fromDate = %s%n", dates.get(i).get(0));
            System.out.printf("toDate = %s%n%n", dates.get(i).get(1));
            PropertyLoader propertyLoader = new PropertyLoader("config.properties");
            String url = String.format("http://ws.audioscrobbler.com/2.0/?method=user.getWeeklyArtistChart&user=%s&api_key=%s&format=json&from=%s&to=%s", propertyLoader.getProperty(USERNAME), propertyLoader.getProperty(LASTFM_API_KEY), fromDate, toDate);
            String json = IOUtils.toString(new URL(url));
            JSONObject obj = new JSONObject(json);
            JSONObject weeklyartistchart = obj.getJSONObject("weeklyartistchart");
            JSONArray artistArray = weeklyartistchart.getJSONArray("artist");
            for (int j = 0; j < artistArray.length(); j++) {
                JSONObject artist = artistArray.getJSONObject(j);
                String name = artist.getString("name");
                int playcount = artist.getInt("playcount");
                String image = null;
                if (artistImageCache.containsKey(name)) {
                    System.out.println("Loading from cache..." + name);
                    image = artistImageCache.get(name);
                } else {
                    try {
                        image = SpotifyAPI.getArtistImage(name);
                        artistImageCache.put(name, image);
                    } catch (JSONException e) {
                        artistImageCache.put(name, null);
                        e.printStackTrace();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
                Artist a = new Artist(name, image, playcount);
                artists.add(new Artist(name, image, 0));

                int index = artistsTemp.indexOf(a);
                if (index == -1) {
                    artistsTemp.add(a);
                } else {
                    artistsTemp.get(index).addPlaycount(playcount);
                }
            }
            allDatesArtists.add(artistsTemp);
            Set<Artist> set = new HashSet<>(artists);
            artists.clear();
            artists.addAll(set);
        }
        saveCache();
        return allDatesArtists;
    }

    public static List<List<Long>> getWeeklyDates() {
        PropertyLoader propertyLoader = new PropertyLoader("config.properties");
        LocalDate currentDate = LocalDate.now(ZoneId.of("UTC"));
        LocalDate fromDate = currentDate.minus(Long.parseLong(propertyLoader.getProperty(TOTAL_WEEKS)), ChronoUnit.WEEKS);
        List<List<Long>> dates = new ArrayList<>();
        while (fromDate.isBefore(currentDate)) {
            LocalDate toDate = fromDate.plus(1, ChronoUnit.WEEKS);
            ZonedDateTime fromZdt = fromDate.atStartOfDay(ZoneId.of("UTC"));
            ZonedDateTime toZdt = toDate.atStartOfDay(ZoneId.of("UTC"));
            List<Long> dateRange = new ArrayList<>();
            dateRange.add(fromZdt.toInstant().toEpochMilli() / 1000);
            dateRange.add(toZdt.toInstant().toEpochMilli() / 1000);
            dates.add(dateRange);
            fromDate = fromDate.plus(1, ChronoUnit.WEEKS);
        }

        return dates;
    }

    private static void writeExcelFile(List<List<Artist>> artistsByWeek, List<List<Long>> dates) throws IOException {
        Workbook workbook = new XSSFWorkbook();


        Sheet sheet = workbook.createSheet("Top Artists");
        int rowNum = 0;
        int colNum = 0;
        Row row = sheet.createRow(rowNum++);
        Cell cell = row.createCell(colNum++);
        cell.setCellValue("entity.Artist");
        cell = row.createCell(colNum++);
        cell.setCellValue("Image URL");
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (List<Long> date : dates) {
            cell = row.createCell(colNum++);
            cell.setCellValue(sdf.format(new Date(date.get(0) * 1000L)) + " - " + sdf.format(new Date(date.get(1) * 1000L)));
            cal.add(Calendar.MONTH, -1);
        }
        for (Artist artist : artists) {
            colNum = 0;
            row = sheet.createRow(rowNum++);
            cell = row.createCell(colNum);
            cell.setCellValue(artist.getName());
            cell = row.createCell(colNum + 1);
            cell.setCellValue(artist.getImage());
        }
        colNum = 1;
        for (List<Artist> artistList : artistsByWeek) {
            for (Artist artist : artistList) {
                int index = artists.indexOf(artist);
                rowNum = index + 1;
                row = sheet.getRow(rowNum);
                if (row == null) {
                    row = sheet.createRow(rowNum);
                }
                int playcountIndex = colNum + 1;
                cell = row.createCell(playcountIndex);
                if (artist.getPlaycounts().size() > 0) {
                    cell.setCellValue(String.valueOf(artist.getPlaycounts().get(0)));
                }
            }
            colNum++;
        }
        PropertyLoader propertyLoader = new PropertyLoader("config.properties");
        try (FileOutputStream out = new FileOutputStream(propertyLoader.getProperty(EXCEL_FILE))) {
            workbook.write(out);
        }
        workbook.close();
    }

    private static void saveCache() throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(CACHE_FILE); ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(artistImageCache);
        }
    }

    private static void loadCache() throws IOException, ClassNotFoundException {
        File file = new File("artistImageCache.ser");
        if (!file.exists()) {
            HashMap<String, String> artistImageCache = new HashMap<>();
            try (FileOutputStream fos = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(artistImageCache);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileInputStream fileIn = new FileInputStream(CACHE_FILE); ObjectInputStream in = new ObjectInputStream(fileIn)) {
                artistImageCache = (Map<String, String>) in.readObject();
            }
        }
    }


}