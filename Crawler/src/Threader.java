import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.google.gson.*;

import java.io.*;
import java.util.*;

// Dette er en Threader klasse. Den får et link fra Crawleren. Hvis Crawleren 

public class Threader implements Runnable {
    private final List<String> batch;
    private String mainURL = "https://kurser.dtu.dk";
    private Crawler parent;
    private String subPage;
    private Map<String, String> cookies;
    private String[] lglPlacements = { "E1A", "E2A", "E3A", "E4A", "E5A", "E1B", "E2B", "E3B", "E4B", "E5B", "E7",
            "F1A", "F2A", "F3A", "F4A", "F5A", "F1B", "F2B", "F3B", "F4B", "F5B", "F7", "Januar", "August", "Juni" };
    private String[] lglTypes = { "Bachelor", "Deltidsdiplom", "Diplom", "Deltidsmaster", "Ph.d.", "Kandidat" };
    private ArrayList<HashMap<String, String>> coursesList = new ArrayList<>();

    public Threader(Crawler parent, List<String> batch, Map<String, String> cookies) {
        this.batch = batch;
        this.parent = parent;
        this.cookies = cookies;
    }

    public void run() {
            for (String page : batch){
                try {
                
                    // System.out.println("Visiting " + page);
                    if (batch.isEmpty()) {
                        break; // Exit if no links are left
                    }
                    // Connect til første link

                    long fetchStartTime = System.currentTimeMillis();
                    Document coursePageData = Jsoup.connect(mainURL + page)
                            .userAgent(
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .cookies(cookies)
                            .get();
                    long fetchEndTime = System.currentTimeMillis();
                    System.out.println("Network fetch time for " + page + ": " + (fetchEndTime - fetchStartTime) + " ms.");


                    long fetchStartTimeParse = System.currentTimeMillis();
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
                        // System.out.println("Checking" + searchword);
                        if (tdPlacement.text().contains(searchword)) {
                            // System.out.println("Found" + searchword);
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
                    System.out.println(courseDetails);

                
                
            } catch (Exception e) {
                System.err.println("Error processing " + subPage + ": " + e.getMessage());
            }
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
}
