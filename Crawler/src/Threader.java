// ╔══════════════════════════════════════════════════════════════════════════╗
// ║                              IMPORTS                                     ║
// ╚══════════════════════════════════════════════════════════════════════════╝

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.google.gson.*;
import okhttp3.*; // Version okhttp-4.11.0
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import kotlin.*; // Version kotlin-stdlib-1.6.20
import okio.Buffer; // Version okio-3.5.0 og okio-jvm-3.5.0



// ╔══════════════════════════════════════════════════════════════════════════════╗
// ║                                THREADER-KLASSE                               ║
// ║ Denne klasse håndterer en batch af links fra en Crawler i separate tråde.    ║
// ║ Den henter og analyserer data fra kursussider og gemmer dem som JSON.        ║
// ╚══════════════════════════════════════════════════════════════════════════════╝


public class Threader implements Runnable {

    // ╔══════════════════════════════════════════════════════════════════════════╗
    // ║                    FELTER TIL OPBEVARING AF DATA                         ║
    // ╚══════════════════════════════════════════════════════════════════════════╝

    private final List<String> batch;                   // Batch af links, der skal behandles
    private String mainURL = "https://kurser.dtu.dk";   // Hoved-URL til kurser
    private final OkHttpClient httpClient;              // HTTP-klient til netværksforbindelser
    private Map<String, String> cookies;                // Cookies til forespørgsler
    private CountDownLatch latch;                       // Synkroniseringsmekanisme til tråde
    private Crawler crawler;                            // Reference til Crawler for progression
    private String[] lglPlacements = { "E1A", "E2A", "E3A", "E4A", "E5A", "E1B", "E2B", "E3B", 
                                       "E4B", "E5B", "E7", "E1", "E2", "E3", "E4", "E5", "E6", 
                                       "F1A", "F2A", "F3A", "F4A", "F5A", "F1B", "F2B", "F3B", 
                                       "F4B", "F5B", "F7", "F1", "F2", "F3", "F4", "F5", "F6",
                                       "Januar", "August", "Juni", "Efterår", "Forår" };

    private String[] lglTypes = { "Bachelor", "Deltidsdiplom", "Diplom", "Deltidsmaster", "Ph.d.", "Kandidat" };

    private ArrayList<HashMap<String, String>> coursesList = new ArrayList<>(); // Liste over kursusdata

    private ArrayList<String> failedList = new ArrayList<>(); // Liste over mislykkede forespørgsler
    private final Object fileLock = new Object();
    private final Set<String> processedPages = Collections.synchronizedSet(new HashSet<>());



    // ╔══════════════════════════════════════════════════════════════════════════╗
    // ║                           KONSTRUKTØR                                    ║
    // ║ Initialiserer batch, cookies, latch, og HTTP-klienten.                   ║
    // ╚══════════════════════════════════════════════════════════════════════════╝


    public Threader(List<String> batch, Map<String, String> cookies, CountDownLatch latch, Crawler crawler) {
        this.batch = batch;
        this.cookies = cookies;
        this.latch = latch;
        this.crawler = crawler;

        // Initialiser HTTP-klienten med timeout-indstillinger
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    // ╔══════════════════════════════════════════════════════════════════════════╗
    // ║                           RUN-METODEN                                    ║
    // ║ Behandler alle links i batchen, henter data og analyserer siderne.       ║
    // ╚══════════════════════════════════════════════════════════════════════════╝

    public void run() {
        // Iterér over hvert link i batchen
        for (String page : batch) {
            if (batch.isEmpty()) {
                System.out.println("Batch er tom.");
                break; // Afbryd, hvis der ikke er flere links
            }
            fetchAndProcessPage(page);
        }
        // Forsøg at behandle de fejlede links igen
        parseFailedLinks();
    }

    // Metode der henter og behandler en side
    private void fetchAndProcessPage(String page) {
        if (processedPages.contains(page)) {
            return; // Skip already processed pages
        }
        processedPages.add(page);

        // Byg en HTTP-forespørgsel for det nuværende link
        Request request = buildRequest(mainURL + page);

        // Send forespørgslen og håndter svar
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleFailure(page, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        handleFailure(page, new IOException("Fejl i svar for " + page + ": " + response.message()));
                        return;
                    }

                    // Hent HTML-indhold fra svaret
                    String responseBody = response.body().string();
                    // Analyser HTML og opdater progression
                    parsePage(responseBody, page);
                    crawler.percentageCalc();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // Reducer latch-tælleren
                }
            }
        });
    }

    private void handleFailure(String page, IOException e) {
        // Log fejl og reducer latch-tælleren
        System.err.println("Kunne ikke hente " + page + ": " + e.getMessage());
        synchronized (failedList) {
            failedList.add(page);
        }
        latch.countDown();
        e.printStackTrace();
    }

    // Metode der parser de fejlslagne links
    public void parseFailedLinks() {
        if (failedList.isEmpty()) {
            System.out.println("Ingen links kunne hentes.");
            return; // Afbryd, hvis der ikke er flere links
        }
        for (String page : failedList) {
            fetchAndProcessPage(page);
        }
    }

    // ╔══════════════════════════════════════════════════════════════════════════╗
    // ║                     HENT LISTE OVER KURSER                               ║
    // ║ Returnerer en kopi af listen over kursusdata (synkroniseret).            ║
    // ╚══════════════════════════════════════════════════════════════════════════╝

    public synchronized ArrayList<HashMap<String, String>> getCoursesList() {
        return new ArrayList<>(coursesList);
    }

    // ╔══════════════════════════════════════════════════════════════════════════╗
    // ║                   GEM DATA SOM JSON                                      ║
    // ║ Appender kursusdetaljer til en JSON-fil for vedvarende lagring.          ║
    // ╚══════════════════════════════════════════════════════════════════════════╝

    public synchronized void appendJson(HashMap<String, String> courseDetails) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
        synchronized (fileLock) { // Brug lås for trådsikkerhed
            try {
                // Læs eksisterende data
                List<HashMap<String, String>> data = Sorter.getData();
                if (data == null) {
                    data = new ArrayList<>(); // Start med tom liste, hvis filen er tom
                }
    
                // Tilføj det nye kursus
                data.add(courseDetails);
    
                // Overskriv filen med hele den opdaterede liste
                try (FileWriter writer = new FileWriter("courses.json")) {
                    writer.write(gson.toJson(data)); // Gem som gyldigt JSON-array
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ╔══════════════════════════════════════════════════════════════════════════╗
    // ║                     BYG HTTP-FORESPØRGSEL                                ║
    // ║ Opretter en GET-forespørgsel med cookies og nødvendige headers.          ║
    // ╚══════════════════════════════════════════════════════════════════════════╝

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


    // ╔══════════════════════════════════════════════════════════════════════════╗
    // ║                     ANALYSER HTML-SIDE                                   ║
    // ║ Ekstraher kursusdata fra HTML og gem det som JSON.                       ║
    // ╚══════════════════════════════════════════════════════════════════════════╝

    private void parsePage(String html, String page) {

        Document coursePageData = Jsoup.parse(html); // Parse HTML med JSoup

        // Håndter information fra side
        HashMap<String, String> courseDetails = new HashMap<>();
        String tdNameAndNumber = coursePageData.selectFirst("h2").text();
        String courseNumber = tdNameAndNumber.substring(0, 5);
        String courseName = tdNameAndNumber.substring(6, tdNameAndNumber.length());
        courseDetails.put("number", courseNumber);
        courseDetails.put("name", courseName);

        Element tdPlacement = coursePageData.select("tr:has( td:has(label a:contains(Skemaplacering)))").first().select("td").get(1);
        Element tdECTS = coursePageData.select("tr:has( td:has(label:contains(ECTS)))").first().select("td").get(1);
        Element tdType = coursePageData.select("tr:has( td:has(label:contains(Kursustype)))").first().select("td").get(1);
        Element tdInstitute = coursePageData.select("tr:has( td:has(label:matchesOwn(^Institut$)))").first().select("td").get(1);


        // Match gyldige placeringer og typer
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


        // Tilføj kursusdetaljer til listen (synkroniseret)
        synchronized (coursesList) {
            coursesList.add(courseDetails); 
        }

        
        appendJson(courseDetails);
    }
}