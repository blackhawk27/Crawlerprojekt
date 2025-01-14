import org.jsoup.Jsoup; // Bibliotek til at hente og parse HTML
import org.jsoup.nodes.Document; // Repræsenterer hele HTML-dokumentet
import org.jsoup.nodes.Element; // Repræsenterer en individuel HTML-node (f.eks. et tag)
import org.jsoup.select.Elements; // Repræsenterer en samling af Element-objekter

import java.io.IOException; // Håndterer input-output undtagelser
import java.util.HashSet; // En samling til unikke elementer (ingen dubletter)
import java.util.Map; // Interface til nøgle-værdi-par
import java.util.HashMap; // Implementering af Map, hvor nøgler er unikke

public class DTUCrawler3 {
    public static void main(String[] args) {

        // URL til DTU kursussøgning
        String url = "https://kurser.dtu.dk/search?CourseCode=&SearchKeyword=&SchedulePlacement=E1%3BE2%3BE3%3BE4%3BE5%3BE1A%3BE2A%3BE3A%3BE4A%3BE5A%3BE1B%3BE2B%3BE3B%3BE4B%3BE5B%3BE7%3BE&SchedulePlacement=E1%3BE1A%3BE1B&SchedulePlacement=E1A&SchedulePlacement=E1B&SchedulePlacement=E2%3BE2A%3BE2B&SchedulePlacement=E2A&SchedulePlacement=E2B&SchedulePlacement=E3%3BE3A%3BE3B&SchedulePlacement=E3A&SchedulePlacement=E3B&SchedulePlacement=E4%3BE4A%3BE4B&SchedulePlacement=E4A&SchedulePlacement=E4B&SchedulePlacement=E5%3BE5A%3BE5B&SchedulePlacement=E5A&SchedulePlacement=E5B&SchedulePlacement=E7&SchedulePlacement=F1%3BF2%3BF3%3BF4%3BF5%3BF1A%3BF2A%3BF3A%3BF4A%3BF5A%3BF1B%3BF2B%3BF3B%3BF4B%3BF5B%3BF7%3BF&SchedulePlacement=F1%3BF1A%3BF1B&SchedulePlacement=F1A&SchedulePlacement=F1B&SchedulePlacement=F2%3BF2A%3BF2B&SchedulePlacement=F2A&SchedulePlacement=F2B&SchedulePlacement=F3%3BF3A%3BF3B&SchedulePlacement=F3A&SchedulePlacement=F3B&SchedulePlacement=F4%3BF4A%3BF4B&SchedulePlacement=F4A&SchedulePlacement=F4B&SchedulePlacement=F5%3BF5A%3BF5B&SchedulePlacement=F5A&SchedulePlacement=F5B&SchedulePlacement=F7&SchedulePlacement=January&SchedulePlacement=August%3BJuly%3BJune&SchedulePlacement=August&SchedulePlacement=July&SchedulePlacement=June&Department=1&Department=10&Department=12&Department=13&Department=22&Department=23&Department=24&Department=25&Department=26&Department=27&Department=28&Department=29&Department=30&Department=33&Department=34&Department=36&Department=38&Department=41&Department=42&Department=46&Department=47&Department=59&Department=IHK&Department=83&CourseType=&TeachingLanguage=";

        // Cookies, der skal bruges til at autentificere og hente siden
        Map<String, String> cookies = new HashMap<>();
        cookies.put("ASP.NET_SessionId", "xmdsqjrkklr3jioup5nnxps2"); // Session-cookie
        cookies.put("SRV_ID", "1"); // Server-cookie

        try {

            // Hent HTML-dokumentet fra URL'en ved hjælp af Jsoup
            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // Browserens
                                                                                                                                                   // brugeragent
                    .cookies(cookies) // Tilføj cookies
                    .get(); // Hent HTML-dokumentet

            // Vælg alle links i <td>-celler, der indeholder href-attributter
            Elements courseLinks = doc.select("td a[href]");

            // Brug HashSet til at gemme unikke kursustitler
            HashSet<String> uniqueCourses = new HashSet<>();
            System.out.println("Kursusnumre og navne:");

            // Gennemgå alle fundne links
            for (Element link : courseLinks) {

                String text = link.text(); // Hent tekstindholdet af linket (kursusnummer og -navn)
                uniqueCourses.add(text); // Tilføj teksten til HashSet (fjerner automatisk dubletter)
                System.out.println("Tilføjet til uniqueCourses: " + text); // Debugging: udskriv tilføjede kurser

            }

            // Udskriv det totale antal unikke kurser
            System.out.println("\nAntal unikke kurser: " + uniqueCourses.size());

        }

        catch (IOException e) {

            // Håndterer fejl, hvis der opstår problemer med at hente data fra URL'en
            System.out.println("Kunne ikke hente data fra URL'en: " + e.getMessage());

        }
    }
}
