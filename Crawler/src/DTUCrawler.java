import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DTUCrawler {

    // URL til DTU's kursussøgningsside
    private final String url = "https://kurser.dtu.dk/search?CourseCode=&SearchKeyword=&SchedulePlacement=E1%3BE2%3BE3%3BE4%3BE5%3BE1A%3BE2A%3BE3A%3BE4A%3BE5A%3BE1B%3BE2B%3BE3B%3BE4B%3BE5B%3BE7%3BE&SchedulePlacement=E1%3BE1A%3BE1B&SchedulePlacement=E1A&SchedulePlacement=E1B&SchedulePlacement=E2%3BE2A%3BE2B&SchedulePlacement=E2A&SchedulePlacement=E2B&SchedulePlacement=E3%3BE3A%3BE3B&SchedulePlacement=E3A&SchedulePlacement=E3B&SchedulePlacement=E4%3BE4A%3BE4B&SchedulePlacement=E4A&SchedulePlacement=E4B&SchedulePlacement=E5%3BE5A%3BE5B&SchedulePlacement=E5A&SchedulePlacement=E5B&SchedulePlacement=E7&SchedulePlacement=F1%3BF2%3BF3%3BF4%3BF5%3BF1A%3BF2A%3BF3A%3BF4A%3BF5A%3BF1B%3BF2B%3BF3B%3BF4B%3BF5B%3BF7%3BF&SchedulePlacement=F1%3BF1A%3BF1B&SchedulePlacement=F1A&SchedulePlacement=F1B&SchedulePlacement=F2%3BF2A%3BF2B&SchedulePlacement=F2A&SchedulePlacement=F2B&SchedulePlacement=F3%3BF3A%3BF3B&SchedulePlacement=F3A&SchedulePlacement=F3B&SchedulePlacement=F4%3BF4A%3BF4B&SchedulePlacement=F4A&SchedulePlacement=F4B&SchedulePlacement=F5%3BF5A%3BF5B&SchedulePlacement=F5A&SchedulePlacement=F5B&SchedulePlacement=F7&SchedulePlacement=January&SchedulePlacement=August%3BJuly%3BJune&SchedulePlacement=August&SchedulePlacement=July&SchedulePlacement=June&Department=1&Department=10&Department=12&Department=13&Department=22&Department=23&Department=24&Department=25&Department=26&Department=27&Department=28&Department=29&Department=30&Department=33&Department=34&Department=36&Department=38&Department=41&Department=42&Department=46&Department=47&Department=59&Department=IHK&Department=83&CourseType=&TeachingLanguage=";

    // Cookies, der er nødvendige for adgang
    private final Map<String, String> cookies;

    // Konstruktør der accepterer cookies
    public DTUCrawler(Map<String, String> cookies) {

        this.cookies = cookies;

    }

    public void startCrawling() {
        try {
            // Hent HTML-indhold fra URL'en
            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .cookies(cookies)
                    .get();
            System.out.println(doc);
            /*
             * // Find kursusnumre og navne
             * Elements courseLinks = doc.select("td a[href]");
             * System.out.println("Kursusnumre og navne:");
             * 
             * for (Element link : courseLinks) {
             * String text = link.text(); // Henter tekstindholdet
             * System.out.println(text); // Udskriver kursusinfo
             * }
             */
        } catch (Exception e) {
            System.out.println("Kunne ikke hente data fra URL'en: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Opret cookies
        Map<String, String> cookies = new HashMap<>();
        cookies.put("ASP.NET_SessionId", "your-session-id");
        cookies.put("SRV_ID", "your-server-id");

        // Opret DTUCrawler med cookies
        DTUCrawler crawler = new DTUCrawler(cookies);
        crawler.startCrawling();
    }
}
