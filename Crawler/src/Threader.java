import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.google.gson.*;
import okhttp3.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kotlin.*;
import okio.Buffer;

// Dette er en Threader klasse. Den får et link fra Crawleren. Hvis Crawleren 

public class Threader implements Runnable {
    private final List<String> batch;
    private String mainURL = "https://kurser.dtu.dk";
    private final OkHttpClient httpClient;
    private String subPage;
    private Map<String, String> cookies;
    private CountDownLatch latch;
    private Crawler crawler;
    private String[] lglPlacements = { "E1A", "E2A", "E3A", "E4A", "E5A", "E1B", "E2B", "E3B", "E4B", "E5B", "E7", "E1", "E2", "E3", "E4", "E5", "E6",
            "F1A", "F2A", "F3A", "F4A", "F5A", "F1B", "F2B", "F3B", "F4B", "F5B", "F7", "F1", "F2", "F3", "F4", "F5", "F6",
            "Januar", "August", "Juni", "Efterår", "Forår"};
    private String[] lglTypes = { "Bachelor", "Deltidsdiplom", "Diplom", "Deltidsmaster", "Ph.d.", "Kandidat" };
    private ArrayList<HashMap<String, String>> coursesList = new ArrayList<>();

    public Threader(List<String> batch, Map<String, String> cookies, CountDownLatch latch, Crawler crawler) {
        this.batch = batch;
        this.cookies = cookies;
        this.latch = latch;
        this.crawler = crawler;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void run() {
        for (String page : batch) {
            if (batch.isEmpty()) {
                System.out.println("Batch is empty");
                break; // Exit if no links are left
            }

            Request request = buildRequest(mainURL + page);
            
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.err.println("Failed to fetch " + page + ": " + e.getMessage());
                    latch.countDown();
                    e.printStackTrace();
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    long fetchStartTime = System.currentTimeMillis();
                    
                    if (!response.isSuccessful()) {
                        System.err.println("Failed response for " + page + ": " + response.message());
                        latch.countDown();
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    long fetchEndTime = System.currentTimeMillis();
                    System.out.println("Network fetch time for " + page + ": " + (fetchEndTime - fetchStartTime) + " ms.");

                    try {
                        parsePage(responseBody, page);
                        crawler.percentageCalc();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                    batch.remove(page);
                }
            });
        }
    }

    public synchronized ArrayList<HashMap<String, String>> getCoursesList() {
        return new ArrayList<>(coursesList);
    }

    // Hovedprogram skal slette filen efter at have taget informationen
    public synchronized void appendJson(HashMap<String, String> courseDetails){
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
        try (FileWriter writer = new FileWriter("courses.json", true)) {
            writer.write(gson.toJson(courseDetails) + "\n");
        } // Skriv data til JSON-fil
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private Request buildRequest(String url) {
        // Build an HTTP GET request with cookies and headers
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        StringBuilder cookieHeader = new StringBuilder();
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            cookieHeader.append(cookie.getKey()).append("=").append(cookie.getValue()).append("; ");
        }
        requestBuilder.header("Cookie", cookieHeader.toString());
        return requestBuilder.build();
    }

    private void parsePage(String html, String page){
        long fetchStartTimeParse = System.currentTimeMillis();
        Document coursePageData = Jsoup.parse(html);
        
        // Håndter information fra side
        HashMap<String, String> courseDetails = new HashMap<>();
        String tdNameAndNumber = coursePageData.selectFirst("h2").text();
        String courseNumber = tdNameAndNumber.substring(0, 5);
        String courseName = tdNameAndNumber.substring(6, tdNameAndNumber.length());
        courseDetails.put("number", courseNumber);
        courseDetails.put("name", courseName);

        Element tdPlacement = coursePageData.select("tr:has( td:has(label a:contains(Skemaplacering)))")
                .first().select("td").get(1);
        Element tdECTS = coursePageData.select("tr:has( td:has(label:contains(ECTS)))").first().select("td")
                .get(1);
        Element tdType = coursePageData.select("tr:has( td:has(label:contains(Kursustype)))").first()
                .select("td").get(1);
        Element tdInstitute = coursePageData.select("tr:has( td:has(label:matchesOwn(^Institut$)))").first()
                .select("td").get(1);

        String placements = "";
        for (String searchword : lglPlacements) {
            if (tdPlacement.text().contains(searchword)) {
                placements = placements + "-" + searchword.toString();
            }
        }
        courseDetails.put("placement", placements);

        for (String searchword : lglTypes) {
            if (tdType.text().contains(searchword)) {
                courseDetails.put("type", searchword);
                break;
            }
        }
        courseDetails.put("ECTS", tdECTS.text());
        courseDetails.put("institute", tdInstitute.text());

        synchronized (coursesList) {
            coursesList.add(courseDetails);
        }
        long fetchEndTimeParse = System.currentTimeMillis();
        System.out.println("Parse time for  " + page + ": " + (fetchEndTimeParse - fetchStartTimeParse) + " ms.");
        
        long fetchStartTimeWrite = System.currentTimeMillis();
        appendJson(courseDetails);
        long fetchEndTimeWrite = System.currentTimeMillis();
        System.out.println("Write time for  " + page + ": " + (fetchEndTimeWrite - fetchStartTimeWrite) + " ms.");
    }
}
