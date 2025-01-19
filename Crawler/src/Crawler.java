import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.google.gson.*;

import java.io.*;
import java.util.*;

// ArrayList[{coursenumber: n, coursename:jajifaoef, courseECTS: 10, coursetype: oijdsajara, institute: koaopkawfe, placement: joiapjfjf}, {HELT NYT KURSUS}]



class Crawler {
    private final Map<String, String> cookies;

    public Crawler(Map<String, String> cookies){
        this.cookies = cookies;
    }

    ArrayList<Threader> threaders = new ArrayList<>();


    public String crawl() {

        String mainURL = "https://kurser.dtu.dk";


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
            
            ArrayList<String> subPageReg = new ArrayList<>(); 

            for (Element link : mainHTML){
                String linkText = link.text(); // Det giver String "01001 - Mathemat...."
                // Lav det første register over hjemmesider man skal besøge
                String subPage = link.attr("href");
                subPageReg.add(subPage);
                System.out.println(link.attr("href") + " added to sub page registry");
            }   

             // Vi besøger Course Page ved threads
            int threadCount = 4;
            Thread[] threads = new Thread[threadCount];
            for (int i=0; i<threadCount; i++){
                Threader threader = new Threader(this, subPageReg, cookies);
                threaders.add(threader); // Add threader to the list
                threads[i] = new Thread(threader);
                threads[i].start();

            }

            for (Thread thread : threads) {
                thread.join();
            }

            ArrayList<HashMap<String, String>> allCourses = new ArrayList<>();
            for (Threader threader : threaders) {
                allCourses.addAll(threader.getCoursesList());
        }
            System.out.print(allCourses);
            System.out.println("All links processed.");
                
            // Konverter kursusdata til JSON og gem i fil
            Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Formatter JSON pænt
        
            try (FileWriter writer = new FileWriter("courses.json")) {
                String jsonPath = "courses.json";
            
                gson.toJson(allCourses, writer); // Skriv data til JSON-fil
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

