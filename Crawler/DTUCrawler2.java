import org.jsoup.Jsoup; // Jsoup bruges til at hente og analysere HTML
import org.jsoup.nodes.Document; // Repræsenterer det samlede HTML-dokument
import org.jsoup.nodes.Element; // Repræsenterer en enkelt HTML-node
import org.jsoup.select.Elements; // Indeholder flere Elementer
import com.google.gson.Gson; // Bruges til at konvertere Java-objekter til JSON
import com.google.gson.GsonBuilder; // Bruges til at formatere JSON-output

import java.io.FileWriter; // Bruges til at skrive data til en fil
import java.io.IOException; // Håndtering af input/output-fejl
import java.util.ArrayList; // Liste til lagring af kurser
import java.util.HashMap; // Kort til at lagre kursusoplysninger
import java.util.List; // Liste interface
import java.util.Map; // Kort interface

public class DTUCrawler2 {
    public static void main(String[] args) {

        // URL til DTU's kursussøgningsside
        String url = "https://kurser.dtu.dk/search?CourseCode=&SearchKeyword=&SchedulePlacement=E1%3BE2%3BE3%3BE4%3BE5%3BE1A%3BE2A%3BE3A%3BE4A%3BE5A%3BE1B%3BE2B%3BE3B%3BE4B%3BE5B%3BE7%3BE&SchedulePlacement=E1%3BE1A%3BE1B&SchedulePlacement=E1A&SchedulePlacement=E1B&SchedulePlacement=E2%3BE2A%3BE2B&SchedulePlacement=E2A&SchedulePlacement=E2B&SchedulePlacement=E3%3BE3A%3BE3B&SchedulePlacement=E3A&SchedulePlacement=E3B&SchedulePlacement=E4%3BE4A%3BE4B&SchedulePlacement=E4A&SchedulePlacement=E4B&SchedulePlacement=E5%3BE5A%3BE5B&SchedulePlacement=E5A&SchedulePlacement=E5B&SchedulePlacement=E7&SchedulePlacement=F1%3BF2%3BF3%3BF4%3BF5%3BF1A%3BF2A%3BF3A%3BF4A%3BF5A%3BF1B%3BF2B%3BF3B%3BF4B%3BF5B%3BF7%3BF&SchedulePlacement=F1%3BF1A%3BF1B&SchedulePlacement=F1A&SchedulePlacement=F1B&SchedulePlacement=F2%3BF2A%3BF2B&SchedulePlacement=F2A&SchedulePlacement=F2B&SchedulePlacement=F3%3BF3A%3BF3B&SchedulePlacement=F3A&SchedulePlacement=F3B&SchedulePlacement=F4%3BF4A%3BF4B&SchedulePlacement=F4A&SchedulePlacement=F4B&SchedulePlacement=F5%3BF5A%3BF5B&SchedulePlacement=F5A&SchedulePlacement=F5B&SchedulePlacement=F7&SchedulePlacement=January&SchedulePlacement=August%3BJuly%3BJune&SchedulePlacement=August&SchedulePlacement=July&SchedulePlacement=June&Department=1&Department=10&Department=12&Department=13&Department=22&Department=23&Department=24&Department=25&Department=26&Department=27&Department=28&Department=29&Department=30&Department=33&Department=34&Department=36&Department=38&Department=41&Department=42&Department=46&Department=47&Department=59&Department=IHK&Department=83&CourseType=&TeachingLanguage=";

        // Definerer cookies, der kræves for at tilgå siden
        Map<String, String> cookies = new HashMap<>();
        cookies.put("ASP.NET_SessionId", "xmdsqjrkklr3jioup5nnxps2"); // Session cookie
        cookies.put("SRV_ID", "1"); // Server cookie

        // Liste til at holde kursusdata som mappe
        List<Map<String, String>> courses = new ArrayList<>();

        try {

            // Hent HTML-indholdet fra siden
            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .cookies(cookies)
                    .get();

            // Udvælg kursusnumre og navne fra tabeldata (<td> med <a href>)
            Elements courseLinks = doc.select("td a[href]");

            System.out.println("Kursusnumre og navne:");

            // Iterer gennem alle links og udtræk kursusnummer og navn

            for (Element link : courseLinks) {

                String text = link.text(); // Teksten indeholder kursusnummer og kursusnavn
                System.out.println(text);

                // Split kursusnummer og navn (format: "12345 - Kursusnavn")
                String[] parts = text.split(" - ");

                if (parts.length == 2) {

                    // Opret en mappe med kursusdata
                    Map<String, String> course = new HashMap<>();
                    course.put("course_number", parts[0].trim()); // Kursusnummer
                    course.put("course_name", parts[1].trim()); // Kursusnavn
                    courses.add(course); // Tilføj til listen
                }
            }

            // Konverter kursusdata til JSON og gem i fil
            Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Formatter JSON pænt

            try (FileWriter writer = new FileWriter("Projekt/courses.json")) {

                gson.toJson(courses, writer); // Skriv data til JSON-fil

            }

            System.out.println("Data gemt i courses.json");

        } catch (IOException e) {

            // Håndtering af fejl, hvis data ikke kan hentes
            System.out.println("Kunne ikke hente data fra URL'en: " + e.getMessage());

        }
    }
}
