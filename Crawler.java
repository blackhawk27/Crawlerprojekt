import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.google.*;
import java.io.*;
import java.util.*;

class Crawler {
    public String crawl(HashMap<String, String> cookies) {

        String mainURL = "https://kurser.dtu.dk";
        String[] legalPlacements = {"E1A", "E2A", "E3A", "E4A", "E5A", "E1B", "E2B", "E3B", "E4B", "E5B", "E7", "F1A", "F2A", "F3A", "F4A", "F5A", "F1B", "F2B", "F3B", "F4B", "F5B", "F7" };
        HashMap<String, ArrayList<String>> courseDict = new HashMap<>();
        ArrayList<HashMap<String, ArrayList<String>>> dataArray = new ArrayList<>(); //Voldsomt. Basically i=0 ville være [{course_01001, [Mathematics 1a, E1A]}]

        String url = "https://kurser.dtu.dk/search?CourseCode=&SearchKeyword=&SchedulePlacement=E1%3BE2%3BE3%3BE4%3BE5%3BE1A%3BE2A%3BE3A%3BE4A%3BE5A%3BE1B%3BE2B%3BE3B%3BE4B%3BE5B%3BE7%3BE&SchedulePlacement=E1%3BE1A%3BE1B&SchedulePlacement=E1A&SchedulePlacement=E1B&SchedulePlacement=E2%3BE2A%3BE2B&SchedulePlacement=E2A&SchedulePlacement=E2B&SchedulePlacement=E3%3BE3A%3BE3B&SchedulePlacement=E3A&SchedulePlacement=E3B&SchedulePlacement=E4%3BE4A%3BE4B&SchedulePlacement=E4A&SchedulePlacement=E4B&SchedulePlacement=E5%3BE5A%3BE5B&SchedulePlacement=E5A&SchedulePlacement=E5B&SchedulePlacement=E7&SchedulePlacement=F1%3BF2%3BF3%3BF4%3BF5%3BF1A%3BF2A%3BF3A%3BF4A%3BF5A%3BF1B%3BF2B%3BF3B%3BF4B%3BF5B%3BF7%3BF&SchedulePlacement=F1%3BF1A%3BF1B&SchedulePlacement=F1A&SchedulePlacement=F1B&SchedulePlacement=F2%3BF2A%3BF2B&SchedulePlacement=F2A&SchedulePlacement=F2B&SchedulePlacement=F3%3BF3A%3BF3B&SchedulePlacement=F3A&SchedulePlacement=F3B&SchedulePlacement=F4%3BF4A%3BF4B&SchedulePlacement=F4A&SchedulePlacement=F4B&SchedulePlacement=F5%3BF5A%3BF5B&SchedulePlacement=F5A&SchedulePlacement=F5B&SchedulePlacement=F7&SchedulePlacement=January&SchedulePlacement=August%3BJuly%3BJune&SchedulePlacement=August&SchedulePlacement=July&SchedulePlacement=June&Department=1&Department=10&Department=12&Department=13&Department=22&Department=23&Department=24&Department=25&Department=26&Department=27&Department=28&Department=29&Department=30&Department=33&Department=34&Department=36&Department=38&Department=41&Department=42&Department=46&Department=47&Department=59&Department=IHK&Department=83&CourseType=&TeachingLanguage=";
        try {
            Document rawData = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .cookies(cookies)
            .timeout(100000)
            .get();

            Elements mainHTML = rawData.select("td a[href]"); // Returns an object of Elements type, from table. It is using the ancestor child formatting of input.
            // Vi tager hele linket, så vi har en liste der ser sådan her ud: [01001 - Mathematics 1a (Polytechnical Foundation), 01002 - Mathematics 1b (Polytechnical Foundation),...n]
            // Det er alle links

            int counter = 0;
            for (Element link : mainHTML){
                String linkText = link.text(); // Det giver String "01001 - Mathemat...."

                String[] parts = linkText.split(" - ");// Nu har vi ["01001", "Mathematics 1a"] i en parts String array

                ArrayList<String> courseDetails = new ArrayList<>();

                courseDetails.add(parts[1]); // Vi tilføjer navnet til courseDetails

                 // Vi tilføjer tallet til courseDict. Efter første omgang skulle courseDict = {01001, [Mathematics 1a]}
                if (counter == 0){
                    counter++;
                // Vi besøger Course Page 
                Document coursePageData = Jsoup.connect(mainURL+link.attr("href"))
                                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                                .cookies(cookies)
                                                .timeout(100000)
                                                .get();

                Elements tdCoursePage = coursePageData.select("td");
                System.out.println(tdCoursePage.text());


                for (String searchword : legalPlacements){
                    System.out.println("Checking" + searchword);
                    if (tdCoursePage.text().contains(searchword)){
                        System.out.println("Found" + searchword);
                        courseDetails.add(searchword);
                    }
                }   
                courseDict.put(parts[0], courseDetails);
            }
            }
             System.out.println(courseDict);
                
            //System.out.println(courseDict.get("01001")); // Hallelujah det virker.

        } catch (Exception e){
                // e
            System.out.println(e.toString());
        }
        return "json fil";
    }

    public static void main(String[] args) {
        HashMap<String, String> cookies = new HashMap<>();
        cookies.put("ASP.NET_SessionId", "xmdsqjrkklr3jioup5nnxps2"); // Session cookie
        cookies.put("SRV_ID", "1"); // Server cookie
        Crawler testCrawler = new Crawler();
        testCrawler.crawl(cookies);
    }
}

