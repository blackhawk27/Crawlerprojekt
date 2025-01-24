

/* ╔══════════════════════════════════════════════════════════════════════════════╗
   ║                          ALT-CODES FOR ASCII ART                             ║
   ║  Use these Alt-codes to create beautiful box-style comments in your code:    ║
   ║                                                                              ║
   ║    Alt + 201  = ╔      (Top-left corner)                                     ║
   ║    Alt + 187  = ╗      (Top-right corner)                                    ║
   ║    Alt + 200  = ╚      (Bottom-left corner)                                  ║
   ║    Alt + 188  = ╝      (Bottom-right corner)                                 ║
   ║    Alt + 205  = ═      (Horizontal line)                                     ║
   ║    Alt + 186  = ║      (Vertical line)                                       ║
   ║                                                                              ║
   ╚══════════════════════════════════════════════════════════════════════════════╝ 
*/


// ╔══════════════════════════════════════════════════════════════════════════╗
// ║                              IMPORTS                                     ║
// ╚══════════════════════════════════════════════════════════════════════════╝

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;


// ╔═══════════════════════════════════════════════════════════════════════════════╗
// ║                              COURSE ANALYZER                                  ║
// ║                         Den bedste kode du kan ønske dig!                     ║
// ╚═══════════════════════════════════════════════════════════════════════════════╝



public class CourseAnalyzer {


    /*───────────────────────────────────────────────────────────────────────────────*/
    /*                         FIELDS AND CONSTANTS                                  */
    /*───────────────────────────────────────────────────────────────────────────────*/

    private JProgressBar progressBar;
    public static String[][] data = {};
    public static String[] columnNames = { "Kursus nr. -", "Kursusnavn -", "Skemaplacering -", "ECTS -", "Type -", "Institut -" };
    private DefaultTableModel tableModel;
    private JTable table; // For tabellen
    private JFrame frame; // For hovedvinduet



    /*───────────────────────────────────────────────────────────────────────────────*/
    /*                            MAIN METHOD                                        */
    /*───────────────────────────────────────────────────────────────────────────────*/
    public static void main(String[] args) {
        // Opret en instans af CourseAnalyzer
        CourseAnalyzer analyzer = new CourseAnalyzer();
    
        // Start GUI i en separat tråd
        SwingUtilities.invokeLater(() -> {
            analyzer.createAndShowGUI();
    
            // Start crawleren EFTER at GUI er fuldt initialiseret
            new Thread(() -> {
                HashMap<String, String> cookies = cookieHandler();
                Crawler crawler = new Crawler(cookies, analyzer); // Passér CourseAnalyzer instansen
                crawler.crawl(); // Kald crawler-metoden
            }).start();
        });
    }
    


    /*───────────────────────────────────────────────────────────────────────────────*/
    /*                        GUI CREATION AND SETUP                                 */
    /*───────────────────────────────────────────────────────────────────────────────*/

    private void createAndShowGUI() {

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                            MAIN FRAME SETUP                              ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Opret hovedrammen
        frame = new JFrame("Crawl Kursusbasen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Få skærmens dimensioner
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);
        frame.setLayout(new BorderLayout());

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                            TITLE PANEL                                   ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Titel i et separat panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Crawl Kursusbasen", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLUE);
        titlePanel.add(titleLabel);
        frame.add(titlePanel, BorderLayout.NORTH);


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                             MAIN PANEL                                   ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Hovedpanel til dropdown og tabel
        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                           SIDEBAR PANEL                                  ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Sidebar med vertikalt layout
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(220, frame.getHeight()));
        sidebarPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "Søg og filtrér", 
        TitledBorder.CENTER, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 16), Color.BLUE));


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                            PROGRESS BAR                                  ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Progressbar tekst
        JLabel progressLabel = new JLabel("Crawler database...");
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        progressLabel.setForeground(Color.BLUE);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(200, 25)); // Juster størrelse

        // Tilføj progressLabel og progressBar øverst
        sidebarPanel.add(progressLabel);
        sidebarPanel.add(Box.createVerticalStrut(10)); // Mellemrum
        sidebarPanel.add(progressBar);
        sidebarPanel.add(Box.createVerticalStrut(20)); // Mellemrum


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                        INPUT OG SØG-KNAP                                 ║
        // ╚══════════════════════════════════════════════════════════════════════════╝


        // Inputlabel og tekstfelt
        JLabel inputLabel = new JLabel("Indtast søgetekst:");
        inputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JTextField inputField = new JTextField(15);
        inputField.setMaximumSize(new Dimension(200, 30)); // Fast bredde
        inputField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Søg-knap
        JButton searchButton = new JButton("Søg");
        searchButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchButton.setPreferredSize(new Dimension(100, 30));

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                           DROPDOWN-MENU Skema                            ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Dropdown label
        JLabel dropdownLabelSkema = new JLabel("Vælg placering:");
        dropdownLabelSkema.setAlignmentX(Component.CENTER_ALIGNMENT);
        dropdownLabelSkema.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Dropdown-menu
        JComboBox<String> dropdownSkema = new JComboBox<>(new String[]{""});
        dropdownSkema.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dropdownSkema.setAlignmentX(Component.CENTER_ALIGNMENT);
        dropdownSkema.setMaximumSize(new Dimension(200, 30));

        String[] Institutes = { "E1A", "E2A", "E3A", "E4A", "E5A", "E1B", "E2B", "E3B", 
                                "E4B", "E5B", "E7", "E1", "E2", "E3", "E4", "E5", "E6", 
                                "F1A", "F2A", "F3A", "F4A", "F5A", "F1B", "F2B", "F3B", 
                                "F4B", "F5B", "F7", "F1", "F2", "F3", "F4", "F5", "F6",
                                "Januar", "August", "Juni", "Efterår", "Forår" };

        for (String Institute : Institutes) {

            dropdownSkema.addItem(Institute);

        }



        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                           DROPDOWN-MENU INSTITUT                         ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Dropdown label
        JLabel dropdownLabelInstitut = new JLabel("Vælg institut:");
        dropdownLabelInstitut.setAlignmentX(Component.CENTER_ALIGNMENT);
        dropdownLabelInstitut.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Dropdown-menu
        JComboBox<String> dropdownInstitut = new JComboBox<>(new String[]{""});
        dropdownInstitut.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dropdownInstitut.setAlignmentX(Component.CENTER_ALIGNMENT);
        dropdownInstitut.setMaximumSize(new Dimension(200, 30));

        String[] institutes = {"01 Institut for Matematik og Computer Science", "10 Institut for Fysik"};

        for (String instutute : institutes) {

            dropdownInstitut.addItem(instutute);

        }


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                    TILFØJ KOMPONENTER TIL SIDEBAR                        ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        sidebarPanel.add(inputLabel);
        sidebarPanel.add(Box.createVerticalStrut(10)); // Mellemrum
        sidebarPanel.add(inputField);
        sidebarPanel.add(Box.createVerticalStrut(10)); // Mellemrum
        sidebarPanel.add(searchButton);
        sidebarPanel.add(Box.createVerticalStrut(20)); // Mellemrum
        sidebarPanel.add(dropdownLabelSkema);
        sidebarPanel.add(Box.createVerticalStrut(10)); // Mellemrum
        sidebarPanel.add(dropdownSkema);
        sidebarPanel.add(Box.createVerticalStrut(10)); // Mellemrum
        sidebarPanel.add(dropdownLabelInstitut);
        sidebarPanel.add(Box.createVerticalStrut(10)); // Mellemrum
        sidebarPanel.add(dropdownInstitut);

        // Tilføj sidebar til mainPanel
        mainPanel.add(sidebarPanel, BorderLayout.WEST);



        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                                TABLE                                     ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Opret ikke-redigerbar tabelmodel
        tableModel = new DefaultTableModel(data, columnNames) {

            @Override
            public boolean isCellEditable(int row, int column) {

                return false; // Gør alle celler ikke-redigerbare

            }
        };

        table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false); // Forhindre kolonneflytning
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        
        

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Sørg for at justere kolonnebredden efter GUI'en er blevet vist
        SwingUtilities.invokeLater(() -> adjustColumnWidths(table));
        createClickableColumnHeaders(table, new Sorter());


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                       ACTIONLISTER TIL SØGEFELT OG KNAP                 ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Tilføj ActionListener til søgeknappen
        searchButton.addActionListener(e -> {
            performSearch(inputField.getText().trim());
        });

        // Tilføj ActionListener til søgefeltet for at lytte efter Enter-tasten
        inputField.addActionListener(e -> {
            performSearch(inputField.getText().trim());
        });

        


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║             ACTIONLISTENER TIL DROPDOWN FOR SKEMAPLACERING               ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        dropdownSkema.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Hent den valgte værdi fra dropdown-menuen
                String selectedPlacement = (String) dropdownSkema.getSelectedItem();

                dropdownSkema.removeItem(""); // Fjern den tomme værdi

                // Tjek, om der er valgt en gyldig placering
                if (selectedPlacement != null && !selectedPlacement.isEmpty()) {

                    // Brug den valgte værdi som query
                    Sorter sorter = new Sorter();
                    String[][][] Placements = sorter.searchCourseWithPlacement(selectedPlacement); 

                    if (Placements != null) {

                        // Opdater tabelmodel med resultaterne
                        tableModel.setDataVector(Placements[1], unpackColumnName(Placements[0]));
                        adjustColumnWidths(table);

                    } 
                    
                    else {

                        JOptionPane.showMessageDialog(frame, "Ingen resultater fundet for søgetermen.", "Information", JOptionPane.INFORMATION_MESSAGE);
                    }

                }

            }
        });


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║             ACTIONLISTENER TIL DROPDOWN FOR INSTITUT                     ║
        // ║  Tilpasser visning af lange tekster og viser tooltip med fuld tekst.     ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Tilføj en custom renderer til dropdownInstitut for at håndtere lange tekster
        dropdownInstitut.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // Sæt forkortet tekst, hvis den er for lang
                String text = value != null ? value.toString() : "";
                if (text.length() > 25) { // Hvis teksten er længere end 25 tegn
                    label.setText(text.substring(0, 22) + "..."); // Forkort teksten og tilføj "..."
                } else {
                    label.setText(text); // Brug original tekst
                }

                // Sæt tooltip med fuld tekst
                label.setToolTipText(text);
                return label;
            }
        });

        // Tilføj ActionListener til dropdownInstitut
        dropdownInstitut.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Hent den valgte værdi fra dropdown-menuen
                String selectedInstitute = (String) dropdownInstitut.getSelectedItem();

                dropdownInstitut.removeItem(""); // Fjern den tomme værdi

                // Tjek, om der er valgt et gyldigt institut
                if (selectedInstitute != null && !selectedInstitute.isEmpty()) {

                    // Brug den valgte værdi som query
                    Sorter sorter = new Sorter();
                    String[][][] institutes = sorter.searchCourseWithInstitute(selectedInstitute); 

                    if (institutes != null) {

                        // Opdater tabelmodel med resultaterne
                        tableModel.setDataVector(institutes[1], unpackColumnName(institutes[0]));
                        adjustColumnWidths(table);

                    } else {

                        JOptionPane.showMessageDialog(frame, "Ingen resultater fundet for søgetermen.", "Information", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                          FINAL SETUP                                     ║
        // ╚══════════════════════════════════════════════════════════════════════════╝


        // Vis GUI
        frame.setVisible(true);
    }


    // ╔══════════════════════════════════════════════════════════════════════════╗
    // ║                           SØGEFUNKTION                                   ║
    // ║ Genbruger søgelogik for både knap og Enter-tast.                         ║
    // ╚══════════════════════════════════════════════════════════════════════════╝

    private void performSearch(String input) {

        if (!input.isEmpty()) {

            Sorter sorter = new Sorter();
            String[][][] results = sorter.searchCourses(input); // Kald searchCourses med input

            if (results != null) {

                // Opdater tabelmodel med resultaterne
                tableModel.setDataVector(results[1], unpackColumnName(results[0]));
                adjustColumnWidths(table);

            } 
            
            else {

                JOptionPane.showMessageDialog(frame, "Ingen resultater fundet for søgetermen.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }

        } 
        
        else {

            JOptionPane.showMessageDialog(frame, "Indtast en søgetekst.", "Advarsel", JOptionPane.WARNING_MESSAGE);

        }
    }

    /* ╔══════════════════════════════════════════════════════════════════════════════╗
       ║                           COOKIE HANDLER FUNCTION                            ║
       ║    This function fetches and processes cookies from a given URL and          ║
       ║    returns them as a HashMap<String, String>.                                ║
       ╚══════════════════════════════════════════════════════════════════════════════╝ */

    public static HashMap<String, String> cookieHandler() {

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                     INITIALIZE COOKIES MAP                               ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        HashMap<String, String> cookiesMap = new HashMap<>(); // Opret HashMap til at gemme cookies
        
        
        
        try {

            // ╔══════════════════════════════════════════════════════════════════════╗
            // ║                      CONNECTING TO URL                               ║
            // ╚══════════════════════════════════════════════════════════════════════╝

            String url = "https://kurser.dtu.dk";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());



            // ╔══════════════════════════════════════════════════════════════════════╗
            // ║                      EXTRACTING COOKIES                              ║
            // ╚══════════════════════════════════════════════════════════════════════╝

            List<String> cookies = response.headers().allValues("Set-Cookie");
            Optional<String> sessionId = cookies.stream().filter(cookie -> cookie.startsWith("ASP.NET_SessionId=")).findFirst();
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

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                     RETURN COOKIES MAP                                   ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        return cookiesMap; // Returner HashMap med cookies

    }


    /*───────────────────────────────────────────────────────────────────────────────*/
    /*                        ADJUST COLUMN WIDTHS                                   */
    /*───────────────────────────────────────────────────────────────────────────────*/

    public void adjustColumnWidths(JTable table) {

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                VALIDER TABELLENS FORÆLDREELEMENT                         ║
        // ║ Kontrollér, om tabellen har en gyldig forælder (JViewport)               ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        if (table.getParent() == null || !(table.getParent() instanceof JViewport)) {
            System.err.println("Tabelens forælder er null eller ikke en JViewport.");
            return; // Forlad metoden, hvis der ikke er et gyldigt forældreelement
        }

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                    INITIALISER VARIABLER                                 ║
        // ║ Hent kolonnemodellen og scroll-pane bredden                              ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        TableColumnModel columnModel = table.getColumnModel();
        int tableWidth = table.getParent().getWidth(); // Brug bredden af scroll-pane

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║          HÅNDTER TABELLER UDEN DATA                                      ║
        // ║ Hvis tabellen er tom, fordel kolonnebredden ligeligt                     ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        if (table.getRowCount() == 0) {
            int equalWidth = tableWidth / table.getColumnCount();

            for (int col = 0; col < table.getColumnCount(); col++) {
                columnModel.getColumn(col).setPreferredWidth(equalWidth);
            }
            return; // Returnér, da der ikke er mere at justere
        }

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║          JUSTER KOLONNEBREDDE BASERET PÅ INDHOLD                         ║
        // ║ Beregn den nødvendige bredde for hver kolonne baseret på cellernes indhold║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        int totalContentWidth = 0;
        int[] contentWidths = new int[table.getColumnCount()];

        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxWidth = 75; // Minimum bredde for hver kolonne

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                maxWidth = Math.max(comp.getPreferredSize().width, maxWidth);
            }

            contentWidths[col] = maxWidth + 10; // Tilføj padding
            totalContentWidth += contentWidths[col];
        }

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║       FORDEL OVERSKYDENDE PLADS PROPORTIONALT MELLEM KOLONNERNE          ║
        // ║ Hvis den samlede indholds-bredde er mindre end scroll-pane bredden       ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        if (totalContentWidth < tableWidth) {
            int extraSpace = tableWidth - totalContentWidth;

            for (int col = 0; col < table.getColumnCount(); col++) {
                int additionalWidth = (int) ((double) contentWidths[col] / totalContentWidth * extraSpace);
                int finalWidth = contentWidths[col] + additionalWidth;
                columnModel.getColumn(col).setPreferredWidth(finalWidth);
            }
        } else {
            // Hvis indholdet fylder mere end scroll-pane bredden, brug standardbredder
            for (int col = 0; col < table.getColumnCount(); col++) {
                columnModel.getColumn(col).setPreferredWidth(contentWidths[col]);
            }
        }

        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║          OPDATER TABELLEN OG DEAKTIVÉR AUTORESIZE                        ║
        // ║ Sørg for, at tabellen opdateres korrekt efter ændringer                  ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    



    /*───────────────────────────────────────────────────────────────────────────────*/
    /*                  CREATE CLICKABLE COLUMN HEADERS                              */
    /*───────────────────────────────────────────────────────────────────────────────*/

    public void createClickableColumnHeaders(JTable table, Sorter sorter) {

        JTableHeader header = table.getTableHeader();

        header.addMouseListener(new MouseAdapter() {

            private boolean ascending = true; // Standard sorteringsretning
            private int lastSortedColumn = -1; // Holder styr på sidst sorterede kolonne

            @Override
            public void mouseClicked(MouseEvent e) {


                // ╔══════════════════════════════════════════════════════════════════╗
                // ║                DETECT COLUMN CLICK AND SORT TABLE                ║
                // ╚══════════════════════════════════════════════════════════════════╝

                // Find kolonneindeks der blev klikket på
                int colIndex = table.columnAtPoint(e.getPoint());

                // Hent data fra tabellen
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                String[][] currentData = getTableDataFromModel(model);

                // Hvis der klikkes på en ny kolonne, skal sorteringsretningen altid starte med pil ned
                if (colIndex != lastSortedColumn) {

                    ascending = true;               // Start altid med stigende sortering for ny kolonne
                    lastSortedColumn = colIndex;    // Opdater sidst sorterede kolonne

                }

                // Sortér data med `Sorter`-metoden
                String[][][] sortedData = sorter.sortTable(currentData, colIndex, ascending);

                // Opdater kolonnenavne med sorteringssymboler
                String[] tempColumnName = unpackColumnName(sortedData[0]);

                for (int i = 0; i < tempColumnName.length; i++) {

                    if (i == colIndex) {

                        tempColumnName[i] = ascending ? tempColumnName[i].replace(" -", " ▼") : tempColumnName[i].replace(" -", " ▲");

                    }

                    else {

                        tempColumnName[i] = tempColumnName[i].substring(0, tempColumnName[i].length() - 2) + " -";

                    }
                }

                // Opdater tabelmodel med sorterede data
                model.setDataVector(sortedData[1], tempColumnName);

                // Juster kolonnebredder
                adjustColumnWidths(table);

                // Skift sorteringsretning for næste klik
                ascending = !ascending;
            }
        });
    }


    /*───────────────────────────────────────────────────────────────────────────────*/
    /*                      GET TABLE DATA AS STRING ARRAY                           */
    /*───────────────────────────────────────────────────────────────────────────────*/

    // Hent data fra JTable-model som String[][]
    private String[][] getTableDataFromModel(DefaultTableModel model) {

        int rowCount = model.getRowCount();
        int columnCount = model.getColumnCount();
        String[][] tableData = new String[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++) {

            for (int j = 0; j < columnCount; j++) {

                tableData[i][j] = model.getValueAt(i, j).toString();

            }
        }

        return tableData;

    }



    /*───────────────────────────────────────────────────────────────────────────────*/
    /*                          UNPACK COLUMN NAMES                                  */
    /*───────────────────────────────────────────────────────────────────────────────*/

    public String[] unpackColumnName(String[][] array) {
        // Tjek for null eller tomt array
        if (array == null || array.length <= 1) {
            return new String[0]; // Returner tomt array, hvis der ikke er noget at fjerne
        }

        // Opret et nyt array uden den første række
        String[] newArray = new String[array.length];

        // Kopier rækkerne undtagen den første
        for (int i = 0; i < array.length; i++) {

            for (int j = 0; j < 1; j++) {
                newArray[i] = array[i][j];
            }

        }

        return newArray;

    }

    /*───────────────────────────────────────────────────────────────────────────────*/
    /*                          UPDATE PROGRESSBAR VALUE                             */
    /*───────────────────────────────────────────────────────────────────────────────*/


    public void updateProgress(int progress, String statusMessage) {

        SwingUtilities.invokeLater(() -> {

            progressBar.setValue(progress);

        });
    }


}
