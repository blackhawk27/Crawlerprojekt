import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.google.gson.*;
import okhttp3.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

// ArrayList[{coursenumber: n, coursename:jajifaoef, courseECTS: 10, coursetype: oijdsajara, institute: koaopkawfe, placement: joiapjfjf}, {HELT NYT KURSUS}]

class Crawler {
    private final Map<String, String> cookies;
    private int numerator = 0;
    private int totalRequests = 0;

    public Crawler(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    ArrayList<Threader> threaders = new ArrayList<>();

    public String crawl() {

        long totalStartTime = System.currentTimeMillis();

        String mainURL = "https://kurser.dtu.dk";

        String url = "https://kurser.dtu.dk/search?CourseCode=&SearchKeyword=&SchedulePlacement=E1%3BE2%3BE3%3BE4%3BE5%3BE1A%3BE2A%3BE3A%3BE4A%3BE5A%3BE1B%3BE2B%3BE3B%3BE4B%3BE5B%3BE7%3BE&SchedulePlacement=E1%3BE1A%3BE1B&SchedulePlacement=E1A&SchedulePlacement=E1B&SchedulePlacement=E2%3BE2A%3BE2B&SchedulePlacement=E2A&SchedulePlacement=E2B&SchedulePlacement=E3%3BE3A%3BE3B&SchedulePlacement=E3A&SchedulePlacement=E3B&SchedulePlacement=E4%3BE4A%3BE4B&SchedulePlacement=E4A&SchedulePlacement=E4B&SchedulePlacement=E5%3BE5A%3BE5B&SchedulePlacement=E5A&SchedulePlacement=E5B&SchedulePlacement=E7&SchedulePlacement=F1%3BF2%3BF3%3BF4%3BF5%3BF1A%3BF2A%3BF3A%3BF4A%3BF5A%3BF1B%3BF2B%3BF3B%3BF4B%3BF5B%3BF7%3BF&SchedulePlacement=F1%3BF1A%3BF1B&SchedulePlacement=F1A&SchedulePlacement=F1B&SchedulePlacement=F2%3BF2A%3BF2B&SchedulePlacement=F2A&SchedulePlacement=F2B&SchedulePlacement=F3%3BF3A%3BF3B&SchedulePlacement=F3A&SchedulePlacement=F3B&SchedulePlacement=F4%3BF4A%3BF4B&SchedulePlacement=F4A&SchedulePlacement=F4B&SchedulePlacement=F5%3BF5A%3BF5B&SchedulePlacement=F5A&SchedulePlacement=F5B&SchedulePlacement=F7&SchedulePlacement=January&SchedulePlacement=August%3BJuly%3BJune&SchedulePlacement=August&SchedulePlacement=July&SchedulePlacement=June&Department=1&Department=10&CourseType=&TeachingLanguage=";
        try {
            long fetchStartTime = System.currentTimeMillis();
            Document rawData = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .cookies(cookies)
                    .timeout(100000)
                    .get();

            long fetchEndTime = System.currentTimeMillis();
            System.out.println("Time taken for main connection " + (fetchEndTime - fetchStartTime));

            Elements mainHTML = rawData.select("td a[href]"); // Returns an object of Elements type, from table. It is
            ArrayList<String> subPageReg = new ArrayList<>();

            for (Element link : mainHTML) {
                String subPage = link.attr("href");
                subPageReg.add(subPage);
            }

            // Vi besøger Course Page ved threads. Det tager for lang tid med synchronized. Vi laver batches i stedet.
            int threadCount = 1;
            Thread[] threads = new Thread[threadCount];
            // Create batches
            int batchSize = (int) Math.ceil((double) subPageReg.size() / threadCount);
            List<List<String>> batches = new ArrayList<>();
            for (int i = 0; i < subPageReg.size(); i += batchSize) {
                int end = Math.min(i + batchSize, subPageReg.size());
                batches.add(new ArrayList<>(subPageReg.subList(i, end))); // Ensure each batch is a distinct subset
                }
            
            totalRequests = subPageReg.size();
            CountDownLatch latch = new CountDownLatch(totalRequests);

            for (int i = 0; i < threadCount; i++) {
                if (i < batches.size()) {  // Ensure we don't access an index out of bounds
                    List<String> batch = batches.get(i);
                    Threader threader = new Threader(batch, cookies, latch, this);
                    threaders.add(threader); 
                    threads[i] = new Thread(threader);
                    threads[i].start();
                    }
                }

            
            latch.await();
            // Sammenflet resultater
            for (Thread thread : threads) {
                if (thread != null) thread.join();
            }

            ArrayList<HashMap<String, String>> allCourses = new ArrayList<>();
            for (Threader threader : threaders) {
                allCourses.addAll(threader.getCoursesList());
            }

            //System.out.print(allCourses);
            long totalEndTime = System.currentTimeMillis();
            System.out.println("Crawler ran for" + (totalEndTime - totalStartTime) + " ms.");
            System.out.println("All links processed.");

            // Konverter kursusdata til JSON og gem i fil
            //Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Formatter JSON pænt

            //try (FileWriter writer = new FileWriter("courses.json")) {
                String jsonPath = "courses.json";

             //   gson.toJson(allCourses, writer); // Skriv data til JSON-fil
               return jsonPath;
            //}

        }

        catch (Exception e) {
            // e
            System.out.println(e.toString());
            return "Could not.";
        }
    }

    public synchronized double percentageCalc() {
        System.out.println("Progress: " + numerator + "/" + totalRequests);
        return numerator++ / totalRequests;
    }

    public static void main(String[] args) {

        Map<String, String> cookies = new HashMap<>();

        // Skal instantieres for at ikke være tom
        cookies.put("ASP.NET_SessionId", "your-session-id");
        cookies.put("SRV_ID", "your-server-id");

        Crawler testCrawler = new Crawler(cookies);
        testCrawler.crawl();
    }

}
