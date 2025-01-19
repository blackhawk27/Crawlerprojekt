import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

// Dette er en Threader klasse. Den får et link fra Crawleren. Hvis Crawleren 

public class Threader implements Runnable{
    private ArrayList<String> subPageReg;
    private String mainURL = "https://kurser.dtu.dk";
    private Crawler parent;
    private String subPage;
    private Map<String, String> cookies;
    private String[] lglPlacements = {"E1A", "E2A", "E3A", "E4A", "E5A", "E1B", "E2B", "E3B", "E4B", "E5B", "E7", "F1A", "F2A", "F3A", "F4A", "F5A", "F1B", "F2B", "F3B", "F4B", "F5B", "F7", "Januar", "August", "Juni" };
    private String[] lglTypes = {"Bachelor", "Deltidsdiplom", "Diplom", "Deltidsmaster", "Ph.d." ,"Kandidat"};
    private ArrayList<HashMap<String, String>> coursesList = new ArrayList<>();
    
    public Threader(Crawler parent, ArrayList<String> subPageReg, Map<String, String> cookies){
        this.subPageReg = subPageReg;
        this.parent = parent;
        this.cookies = cookies;
    }
    
    public void run(){
        while(true){
            try{
            synchronized (subPageReg) {
                System.out.println("Threads are running");
                if (subPageReg.isEmpty()) {
                    break; // Exit if no links are left
                }
                subPage = subPageReg.remove(0); // Retrieve and remove the first link

                Document coursePageData = Jsoup.connect(mainURL+subPage)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .cookies(cookies)
                .timeout(100000)
                .get();

                HashMap<String, String> courseDetails = new HashMap<>();
                String tdNameAndNumber = coursePageData.selectFirst("h2").text();
                String courseNumber = tdNameAndNumber.substring(0,5);
                String courseName = tdNameAndNumber.substring(6, tdNameAndNumber.length());

                Element tdPlacement = coursePageData.select("tr:has( td:has(label a:contains(Skemaplacering)))").first().select("td").get(1);
                Element tdECTS = coursePageData.select("tr:has( td:has(label:contains(ECTS)))").first().select("td").get(1);
                Element tdType = coursePageData.select("tr:has( td:has(label:contains(Kursustype)))").first().select("td").get(1);
                Element tdInstitute = coursePageData.select("tr:has( td:has(label:matchesOwn(^Institut$)))").first().select("td").get(1);

                String placements = "";
                for (String searchword : lglPlacements){
                    //System.out.println("Checking" + searchword);
                    if (tdPlacement.text().contains(searchword)){
                        //System.out.println("Found" + searchword);
                        placements = placements+"-"+searchword.toString();
                    }
                }
                courseDetails.put("placement", placements);

                for (String searchword : lglTypes){
                    if (tdType.text().contains(searchword)){
                        courseDetails.put("type", searchword);
                        break;
                    }
                }

                courseDetails.put("ECTS", tdECTS.text());
                courseDetails.put("institute", tdInstitute.text());
                
                synchronized (coursesList){
                    coursesList.add(courseDetails);

                }
               
            }
        } catch (Exception e){
            System.out.println(e.toString());

        }
        }
    } 

    public synchronized ArrayList<HashMap<String, String>> getCoursesList() {
        return new ArrayList<>(coursesList);
    }   
}
