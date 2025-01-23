
// ╔══════════════════════════════════════════════════════════════════════════╗
// ║                              IMPORTS                                     ║
// ╚══════════════════════════════════════════════════════════════════════════╝

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.google.gson.*;
import okhttp3.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;


/* ╔══════════════════════════════════════════════════════════════════════════════╗
   ║                                CRAWLER KLASSE                                ║
   ║ Denne klasse håndterer crawlingen af kursussider fra DTU's kursusside.       ║
   ║ Den henter kursusinformation og opdaterer progressionen i CourseAnalyzer-GUI.║
   ╚══════════════════════════════════════════════════════════════════════════════╝ */

class Crawler {


    /* ╔══════════════════════════════════════════════════════════════════════════╗
       ║                       FELTER OG KONSTANTER                               ║
       ╚══════════════════════════════════════════════════════════════════════════╝ */


    private final Map<String, String> cookies;  // Cookies til HTTP-forespørgsle
    private int numerator = 0;                  // Antal behandlede forespørgsler
    private int totalRequests = 0;              // Totalt antal forespørgsler
    private CourseAnalyzer courseAnalyzer;      // Reference til CourseAnalyzer til GUI-opdatering


    /* ╔══════════════════════════════════════════════════════════════════════════╗
       ║                           KONSTRUKTØR                                    ║
       ╚══════════════════════════════════════════════════════════════════════════╝ */

    public Crawler(Map<String, String> cookies, CourseAnalyzer courseAnalyzer) {

        this.cookies = cookies;                 // Gem cookies
        this.courseAnalyzer = courseAnalyzer;   // Gem reference til CourseAnalyzer

    }

    ArrayList<Threader> threaders = new ArrayList<>();


    /* ╔══════════════════════════════════════════════════════════════════════════╗
       ║                             CRAWL-METODE                                 ║
       ║ Denne metode crawler kursussiderne, behandler siderne og opdaterer       ║
       ║ progressionen i GUI'en.                                                  ║
       ╚══════════════════════════════════════════════════════════════════════════╝ */

    public String crawl() {

        // Start tidtagning for hele crawlingen
        long totalStartTime = System.currentTimeMillis();

        String mainURL = "https://kurser.dtu.dk";

        String url = "https://kurser.dtu.dk/search?CourseCode=&SearchKeyword=&SchedulePlacement=E1%3BE2%3BE3%3BE4%3BE5%3BE1A%3BE2A%3BE3A%3BE4A%3BE5A%3BE1B%3BE2B%3BE3B%3BE4B%3BE5B%3BE7%3BE&SchedulePlacement=E1%3BE1A%3BE1B&SchedulePlacement=E1A&SchedulePlacement=E1B&SchedulePlacement=E2%3BE2A%3BE2B&SchedulePlacement=E2A&SchedulePlacement=E2B&SchedulePlacement=E3%3BE3A%3BE3B&SchedulePlacement=E3A&SchedulePlacement=E3B&SchedulePlacement=E4%3BE4A%3BE4B&SchedulePlacement=E4A&SchedulePlacement=E4B&SchedulePlacement=E5%3BE5A%3BE5B&SchedulePlacement=E5A&SchedulePlacement=E5B&SchedulePlacement=E7&SchedulePlacement=F1%3BF2%3BF3%3BF4%3BF5%3BF1A%3BF2A%3BF3A%3BF4A%3BF5A%3BF1B%3BF2B%3BF3B%3BF4B%3BF5B%3BF7%3BF&SchedulePlacement=F1%3BF1A%3BF1B&SchedulePlacement=F1A&SchedulePlacement=F1B&SchedulePlacement=F2%3BF2A%3BF2B&SchedulePlacement=F2A&SchedulePlacement=F2B&SchedulePlacement=F3%3BF3A%3BF3B&SchedulePlacement=F3A&SchedulePlacement=F3B&SchedulePlacement=F4%3BF4A%3BF4B&SchedulePlacement=F4A&SchedulePlacement=F4B&SchedulePlacement=F5%3BF5A%3BF5B&SchedulePlacement=F5A&SchedulePlacement=F5B&SchedulePlacement=F7&SchedulePlacement=January&SchedulePlacement=August%3BJuly%3BJune&SchedulePlacement=August&SchedulePlacement=July&SchedulePlacement=June&Department=1&Department=10&CourseType=&TeachingLanguage=";

        try {

            // Start tidtagning for forbindelse til hovedsiden
            long fetchStartTime = System.currentTimeMillis();

            /* ╔══════════════════════════════════════════════════════════════════════╗
               ║             HENT HTML-DATA FRA HOVEDSIDEN (JSOUP)                    ║
               ║ Vi bruger JSoup til at lave en HTTP GET-forespørgsel til URL'en      ║
               ║ og hente HTML-dokumentet. Dette dokument indeholder kursusdata.      ║
               ╚══════════════════════════════════════════════════════════════════════╝ */

            // Hent hovedsidedata
            Document rawData = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .cookies(cookies)
                    .get();

            // Slut tidtagning for forbindelse
            long fetchEndTime = System.currentTimeMillis();
            System.out.println("Time taken for main connection " + (fetchEndTime - fetchStartTime));


            /* ╔══════════════════════════════════════════════════════════════════════╗
               ║             EKSTRAHER LINKS FRA HOVEDSIDEN                           ║
               ║ Vi udvælger alle links i tabellen, som har "href"-attributter, og    ║
               ║ gemmer dem i en liste for videre behandling.                         ║
               ╚══════════════════════════════════════════════════════════════════════╝ */

            Elements mainHTML = rawData.select("td a[href]"); // Find alle links i tabelcellerne
            ArrayList<String> subPageReg = new ArrayList<>();

            for (Element link : mainHTML) {
                String subPage = link.attr("href"); // Hent href-attributten fra hvert link
                subPageReg.add(subPage); // Tilføj til listen over underlinks
            }

            // ╔════════════════════════════════════════════════════════════════════╗
            // ║                  OPRET OG BEHANDL TRÅDE                            ║
            // ╚════════════════════════════════════════════════════════════════════╝

            /* 
            * Vi opretter et antal tråde for at behandle kursuslinks parallelt.
            * Dette forbedrer ydeevnen ved at hente flere kurser på samme tid.
            */

            int threadCount = 1; // Antal tråde (kan justeres)
            Thread[] threads = new Thread[threadCount];

             /* ╔══════════════════════════════════════════════════════════════════════╗
                ║               OPDEL LINKS I BATCHES FOR TRÅDENE                      ║
                ╚══════════════════════════════════════════════════════════════════════╝ */

            // Create batches
            int batchSize = (int) Math.ceil((double) subPageReg.size() / threadCount); // Antal links pr. batch
            List<List<String>> batches = new ArrayList<>();

            for (int i = 0; i < subPageReg.size(); i += batchSize) {

                int end = Math.min(i + batchSize, subPageReg.size()); // Undgå out-of-bounds fejl
                batches.add(new ArrayList<>(subPageReg.subList(i, end))); // Opret batches

            }

            totalRequests = subPageReg.size(); // Samlet antal links, der skal behandles
            CountDownLatch latch = new CountDownLatch(totalRequests); // Bruges til at vente på, at alle tråde bliver færdige


            /* ╔══════════════════════════════════════════════════════════════════════╗
               ║               START TRÅDE TIL BEHANDLING AF LINKS                    ║
               ╚══════════════════════════════════════════════════════════════════════╝ */

               for (int i = 0; i < threadCount; i++) {
                if (i < batches.size()) { // Sørg for, at der er en batch til denne tråd
                    List<String> batch = batches.get(i); // Hent batch
                    Threader threader = new Threader(batch, cookies, latch, this); // Opret Threader-instans
                    threaders.add(threader); // Gem Threader i listen
                    threads[i] = new Thread(threader); // Opret en ny tråd
                    threads[i].start(); // Start tråden
                    percentageCalc(); // Opdater progressionen
                }
            }

            latch.await(); // Vent på, at alle tråde er færdige

            /* ╔══════════════════════════════════════════════════════════════════════╗
               ║               SAMMENFLET RESULTATER FRA TRÅDE                        ║
               ╚══════════════════════════════════════════════════════════════════════╝ */

            for (Thread thread : threads) {
                if (thread != null)
                    thread.join(); // Vent på, at hver tråd afsluttes
            }

            ArrayList<HashMap<String, String>> allCourses = new ArrayList<>();

            for (Threader threader : threaders) {
                allCourses.addAll(threader.getCoursesList()); // Tilføj resultater fra hver tråd
            }

            // Slut tidtagning for hele processen
            long totalEndTime = System.currentTimeMillis();
            System.out.println("Crawler ran for " + (totalEndTime - totalStartTime) + " ms.");
            System.out.println("All links processed.");

            /* ╔══════════════════════════════════════════════════════════════════════╗
               ║          GEM KURSUSDATA SOM JSON-FIL                                 ║
               ╚══════════════════════════════════════════════════════════════════════╝ */

            String jsonPath = "courses.json";

         
            return jsonPath; // Returner stien til JSON-filen
            

        }

        catch (Exception e) {
            // e
            System.out.println(e.toString());
            return "Could not.";
        }
    }


    /* ╔══════════════════════════════════════════════════════════════════════════╗
       ║                     PROGRESSIONSBEREGNING                                ║
       ║ Denne metode beregner og opdaterer progressionsstatussen i GUI'en.       ║
       ╚══════════════════════════════════════════════════════════════════════════╝ */

       public synchronized double percentageCalc() {
        double progress = (double) ++numerator / totalRequests;

        // Opdater GUI via CourseAnalyzer
        if (courseAnalyzer != null) {
            courseAnalyzer.updateProgress((int) (progress * 100), 
                "Crawling side " + numerator + " af " + totalRequests);
        }

        System.out.println("Progression: " + numerator + "/" + totalRequests);
        return progress;
    }


    /* ╔══════════════════════════════════════════════════════════════════════════╗
       ║                             MAIN-METODE                                  ║
       ║ Programindgangen. Sætter cookies op og starter crawling.                 ║
       ╚══════════════════════════════════════════════════════════════════════════╝ */

    public static void main(String[] args) {

        Map<String, String> cookies = new HashMap<>();

        // Skal instantieres for at ikke være tom
        cookies.put("ASP.NET_SessionId", "your-session-id");
        cookies.put("SRV_ID", "your-server-id");

        // Opret en instans af Crawler med både cookies og progressBar
        Crawler testCrawler = new Crawler(cookies, this);

        // Start crawling
        testCrawler.crawl();
    }

}