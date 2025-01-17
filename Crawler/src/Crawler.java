import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

// ArrayList[{coursenumber: n, coursename:jajifaoef, courseECTS: 10, coursetype: oijdsajara, institute: koaopkawfe, placement: joiapjfjf}, {HELT NYT KURSUS}]



class Crawler {
    private final Map<String, String> cookies;

    public Crawler(Map<String, String> cookies){
        this.cookies = cookies;
    }


    public String crawl() {

        String mainURL = "https://kurser.dtu.dk";
        String[] lglPlacements = {"E1A", "E2A", "E3A", "E4A", "E5A", "E1B", "E2B", "E3B", "E4B", "E5B", "E7", "F1A", "F2A", "F3A", "F4A", "F5A", "F1B", "F2B", "F3B", "F4B", "F5B", "F7", "Januar", "August", "Juni" };
        String[] lglTypes = {"Bachelor", "Deltidsdiplom", "Diplom", "Deltidsmaster", "Ph.d." ,"Kandidat"};


        ArrayList<HashMap<String, String>> coursesList = new ArrayList<>();

        String url = "https://kurser.dtu.dk/search?CourseCode=&SearchKeyword=&SchedulePlacement=E1%3BE2%3BE3%3BE4%3BE5%3BE1A%3BE2A%3BE3A%3BE4A%3BE5A%3BE1B%3BE2B%3BE3B%3BE4B%3BE5B%3BE7%3BE&SchedulePlacement=E1%3BE1A%3BE1B&SchedulePlacement=E1A&SchedulePlacement=E1B&SchedulePlacement=E2%3BE2A%3BE2B&SchedulePlacement=E2A&SchedulePlacement=E2B&SchedulePlacement=E3%3BE3A%3BE3B&SchedulePlacement=E3A&SchedulePlacement=E3B&SchedulePlacement=E4%3BE4A%3BE4B&SchedulePlacement=E4A&SchedulePlacement=E4B&SchedulePlacement=E5%3BE5A%3BE5B&SchedulePlacement=E5A&SchedulePlacement=E5B&SchedulePlacement=E7&SchedulePlacement=F1%3BF2%3BF3%3BF4%3BF5%3BF1A%3BF2A%3BF3A%3BF4A%3BF5A%3BF1B%3BF2B%3BF3B%3BF4B%3BF5B%3BF7%3BF&SchedulePlacement=F1%3BF1A%3BF1B&SchedulePlacement=F1A&SchedulePlacement=F1B&SchedulePlacement=F2%3BF2A%3BF2B&SchedulePlacement=F2A&SchedulePlacement=F2B&SchedulePlacement=F3%3BF3A%3BF3B&SchedulePlacement=F3A&SchedulePlacement=F3B&SchedulePlacement=F4%3BF4A%3BF4B&SchedulePlacement=F4A&SchedulePlacement=F4B&SchedulePlacement=F5%3BF5A%3BF5B&SchedulePlacement=F5A&SchedulePlacement=F5B&SchedulePlacement=F7&SchedulePlacement=January&SchedulePlacement=August%3BJuly%3BJune&SchedulePlacement=August&SchedulePlacement=July&SchedulePlacement=June&Department=1&Department=10&CourseType=&TeachingLanguage=";
        try {
            Document rawData = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .cookies(cookies)
            .timeout(100000)
            .get();

            Elements mainHTML = rawData.select("td a[href]"); // Returns an object of Elements type, from table. It is using the ancestor child formatting of input.
            // Vi tager hele linket, så vi har en liste der ser sådan her ud: [01001 - Mathematics 1a (Polytechnical Foundation), 01002 - Mathematics 1b (Polytechnical Foundation),...n]
            // Det er alle links

            int counter = -1;
            for (Element link : mainHTML){
                String linkText = link.text(); // Det giver String "01001 - Mathemat...."

                String[] parts = linkText.split(" - ");// Nu har vi ["01001", "Mathematics 1a"] i en parts String array

                HashMap<String, String> courseDetails = new HashMap<>();

                courseDetails.put("courseNumber", parts[0]); // Tilføj mummer til coursedetails
                courseDetails.put("courseName", parts[1]); // TIlføj navn til coursedetails

                // Vi besøger Course Page 
                Document coursePageData = Jsoup.connect(mainURL+link.attr("href"))
                                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                                .cookies(cookies)
                                                .timeout(100000)
                                                .get();
                
                // Hiver diverse data ud af kursets side. Her er det placering, ects, kursustyope, institut
                Element tdPlacement = coursePageData.select("tr:has( td:has(label a:contains(Skemaplacering)))").first().select("td").get(1);
                Element tdECTS = coursePageData.select("tr:has( td:has(label:contains(ECTS)))").first().select("td").get(1);
                Element tdType = coursePageData.select("tr:has( td:has(label:contains(Kursustype)))").first().select("td").get(1);
                Element tdInstitute = coursePageData.select("tr:has( td:has(label:matchesOwn(^Institut$)))").first().select("td").get(1);

                System.out.println(tdPlacement.text());
                System.out.println(tdECTS.text());
                System.out.println(tdType.text());
                System.out.println(tdInstitute.text());

                String placements = "";
                for (String searchword : lglPlacements){
                    System.out.println("Checking" + searchword);
                    if (tdPlacement.text().contains(searchword)){
                        System.out.println("Found" + searchword);
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
                
                coursesList.add(courseDetails);
             
            
            
            }
             System.out.println(coursesList);
                
            // Konverter kursusdata til JSON og gem i fil
            Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Formatter JSON pænt

            try (FileWriter writer = new FileWriter("courses.json")) {
                String jsonPath = "courses.json";
            
                gson.toJson(coursesList, writer); // Skriv data til JSON-fil
                return jsonPath;
                }
            
        } catch (Exception e){
                // e
            System.out.println(e.toString());
            return "Could not.";
        }
        
    }

    public static void main(String[] args) {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("ASP.NET_SessionId", "xmdsqjrkklr3jioup5nnxps2"); // Session cookie
        cookies.put("SRV_ID", "1"); // Server cookie
        Crawler testCrawler = new Crawler(cookies);
        testCrawler.crawl();
    }
}

