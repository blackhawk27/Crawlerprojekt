/*

Venligst opdater class og metode navne, men test metoder og classes er inkluderet i src mappen :)

*/



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CourseAnalyzer {

    public static void main(String[] args) {
        // Start GUI i en separat tråd
        SwingUtilities.invokeLater(() -> new CourseAnalyzer().createAndShowGUI());

        HashMap<String, String> cookies = cookieHandler();

        // Start crawler i en separat tråd
        new Thread(() -> {
            // Antag at din Crawler klasse hedder "DTUCrawler"
            DTUCrawler crawler = new DTUCrawler(cookies);
            crawler.startCrawling(); // Kald crawler-metoden
        }).start();

        // Udskriv værdierne fra HashMap
        System.out.println("ASP.NET_SessionId: " + cookies.get("ASP.NET_SessionId"));
        System.out.println("SRV_ID: " + cookies.get("SRV_ID"));
    }

    private void createAndShowGUI() {

        // Opret hovedrammen
        JFrame frame = new JFrame("Crawl Kursusbasen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // Titel i et separat panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Crawl Kursusbasen", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLUE);
        titlePanel.add(titleLabel);
        frame.add(titlePanel, BorderLayout.NORTH);

        // Hovedpanel til dropdown og tabel
        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);

        // Sidebar med dropdown-menu
        JPanel sidebarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Vandret layout
        sidebarPanel.setPreferredSize(new Dimension(200, frame.getHeight())); // Fast bredde
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 0)); // Margen

        JLabel dropdownLabel = new JLabel("Vælg placering:");
        dropdownLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JComboBox<String> dropdown = new JComboBox<>(new String[] { "Placering" });
        dropdown.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Tilføj dropdown-menuen og label til sidebar
        sidebarPanel.add(dropdownLabel);
        sidebarPanel.add(dropdown);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Tabel i centrum
        String[][] data = {}; // Tom data ved start
        String[] columnNames = { "Kursusnummer", "Navn", "Placering", "ECTS", "Institut" };

        // Opret ikke-redigerbar tabelmodel
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Gør alle celler ikke-redigerbare
            }
        };

        JTable table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false); // Forhindre kolonneflytning

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ActionListener til dropdown
        dropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dropdown.getSelectedItem().equals("Placering")) {

                    // Kald din Sort-klasse for at hente placeringer
                    Sorter_Test sorter = new Sorter_Test();
                    String[][] placements = sorter.getCoursesByPlacement();

                    tableModel.setDataVector(placements, columnNames);
                }
            }
        });

        // Vis GUI
        frame.setVisible(true);
    }

    public static HashMap<String, String> cookieHandler() {

        HashMap<String, String> cookiesMap = new HashMap<>(); // Opret HashMap til at gemme cookies

        try {

            String url = "https://kurser.dtu.dk";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            List<String> cookies = response.headers().allValues("Set-Cookie");

            Optional<String> sessionId = cookies.stream().filter(cookie -> cookie.startsWith("ASP.NET_SessionId="))
                    .findFirst();

            Optional<String> serverId = cookies.stream().filter(cookie -> cookie.startsWith("SRV_ID=")).findFirst();

            if (sessionId.isPresent()) {

                String rawCookie = sessionId.get();
                String sessionIdValue = rawCookie.split(";")[0].split("=")[1];
                cookiesMap.put("ASP.NET_SessionId", sessionIdValue);

                // System.out.println("ASP.NET_SessionId: " + sessionIdValue);

            }

            else {

                System.out.println("Ingen ASP.NET_SessionId fundet");

            }

            if (serverId.isPresent()) {

                String rawCookie = serverId.get();
                String serverIdValue = rawCookie.split(";")[0].split("=")[1];
                cookiesMap.put("SRV_ID", serverIdValue);

                // System.out.println("SRV_ID: " + serverIdValue);

            }

            else {
                System.out.println("Ingen SRV_ID fundet");
            }

        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return cookiesMap; // Returner HashMap med cookies

    }

}
