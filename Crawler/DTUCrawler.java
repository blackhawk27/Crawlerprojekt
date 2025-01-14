import org.jsoup.Jsoup; // Jsoup bruges til at hente og parse HTML-indhold
import org.jsoup.nodes.Document; // Repræsenterer hele HTML-dokumentet
import org.jsoup.nodes.Element; // Repræsenterer en enkelt HTML-element
import org.jsoup.select.Elements; // Repræsenterer en samling af HTML-elementer
import java.io.IOException; // Håndterer IO-fejl
import java.util.HashMap; // En implementering af en map, der lagrer nøgleværdipar
import java.util.Map; // Interface til maps

public class DTUCrawler {

    public static void main(String[] args) {

        // URL til DTU's kursussøgningsside med query-parametre
        String url = "https://kurser.dtu.dk/search?CourseCode=&SearchKeyword=&SchedulePlacement=E1%3BE2%3BE3%3BE4%3BE5%3BE1A%3BE2A%3BE3A%3BE4A%3BE5A%3BE1B%3BE2B%3BE3B%3BE4B%3BE5B%3BE7%3BE&SchedulePlacement=E1%3BE1A%3BE1B&SchedulePlacement=E1A&SchedulePlacement=E1B&SchedulePlacement=E2%3BE2A%3BE2B&SchedulePlacement=E2A&SchedulePlacement=E2B&SchedulePlacement=E3%3BE3A%3BE3B&SchedulePlacement=E3A&SchedulePlacement=E3B&SchedulePlacement=E4%3BE4A%3BE4B&SchedulePlacement=E4A&SchedulePlacement=E4B&SchedulePlacement=E5%3BE5A%3BE5B&SchedulePlacement=E5A&SchedulePlacement=E5B&SchedulePlacement=E7&SchedulePlacement=F1%3BF2%3BF3%3BF4%3BF5%3BF1A%3BF2A%3BF3A%3BF4A%3BF5A%3BF1B%3BF2B%3BF3B%3BF4B%3BF5B%3BF7%3BF&SchedulePlacement=F1%3BF1A%3BF1B&SchedulePlacement=F1A&SchedulePlacement=F1B&SchedulePlacement=F2%3BF2A%3BF2B&SchedulePlacement=F2A&SchedulePlacement=F2B&SchedulePlacement=F3%3BF3A%3BF3B&SchedulePlacement=F3A&SchedulePlacement=F3B&SchedulePlacement=F4%3BF4A%3BF4B&SchedulePlacement=F4A&SchedulePlacement=F4B&SchedulePlacement=F5%3BF5A%3BF5B&SchedulePlacement=F5A&SchedulePlacement=F5B&SchedulePlacement=F7&SchedulePlacement=January&SchedulePlacement=August%3BJuly%3BJune&SchedulePlacement=August&SchedulePlacement=July&SchedulePlacement=June&Department=1&Department=10&Department=12&Department=13&Department=22&Department=23&Department=24&Department=25&Department=26&Department=27&Department=28&Department=29&Department=30&Department=33&Department=34&Department=36&Department=38&Department=41&Department=42&Department=46&Department=47&Department=59&Department=IHK&Department=83&CourseType=&TeachingLanguage=";

        // Cookies, der er nødvendige for at få adgang til siden
        Map<String, String> cookies = new HashMap<>();

        cookies.put("ASP.NET_SessionId", "xmdsqjrkklr3jioup5nnxps2"); // Eksempelværdi, erstat med din faktiske
                                                                      // session-id

        cookies.put("SRV_ID", "1"); // Eksempelværdi, erstat med korrekt værdi

        try {
            // Hent HTML-indholdet fra URL'en med brugeragent og cookies
            Document doc = Jsoup.connect(url)

                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // Brugeragent
                                                                                                                                                   // for
                                                                                                                                                   // at
                                                                                                                                                   // simulere
                                                                                                                                                   // en
                                                                                                                                                   // Chrome-browser
                    .cookies(cookies) // Tilføjer cookies til anmodningen
                    .get(); // Sender GET-anmodningen og henter HTML-indholdet

            // Udskriv HTML-indholdet til fejlsøgning (kan aktiveres, hvis du vil
            // kontrollere den hentede HTML)
            // System.out.println("HTML-indhold:");
            System.out.println(doc.html());

            // Find kursusnumre og navne ved at søge efter hyperlinks i tabeller
            Elements courseLinks = doc.select("td a[href]"); // Vælger alle <a>-tags i <td>-celler

            /*
             * System.out.println("Kursusnumre og navne:");
             * 
             * 
             * for (Element link : courseLinks) { // Itererer gennem alle de fundne links
             * 
             * String text = link.text(); // Henter tekstindholdet i linket (kursusnummer og
             * navn)
             * System.out.println(text); // Udskriver tekstindholdet
             * }
             */
        }

        catch (IOException e) { // Håndterer undtagelser, hvis der opstår fejl under hentning af data
            System.out.println("Kunne ikke hente data fra URL'en: " + e.getMessage()); // Udskriver fejlmeddelelse
        }
    }
}
