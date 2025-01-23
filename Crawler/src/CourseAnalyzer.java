

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
    public static String[][] data;
    public static String[] columnNames = { "Kursus nr. -", "Kursusnavn -", "Skemaplacering -", "ECTS -", "Type -",
            "Institut -" };



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
        JFrame frame = new JFrame("Crawl Kursusbasen");
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

        // Sidebar med dropdown-menu
        JPanel sidebarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Vandret layout
        sidebarPanel.setPreferredSize(new Dimension(200, frame.getHeight())); // Fast bredde
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10)); // Margen

        JLabel inputLabel = new JLabel("Indtast søgetekst:");
        inputLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JTextField inputField = new JTextField(15); // Inputfelt med bredde
        inputField.setMaximumSize(new Dimension(200, 30)); // Begræns bredde

        JButton searchButton = new JButton("Søg");
        searchButton.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel dropdownLabel = new JLabel("Vælg placering:");
        dropdownLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JComboBox<String> dropdown = new JComboBox<>(new String[] { "Placering" });
        dropdown.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Tilføj inputfelt og søgeknap
        sidebarPanel.add(inputLabel);
        sidebarPanel.add(inputField);
        sidebarPanel.add(searchButton);

        // Tilføj dropdown-menuen og label til sidebar
        sidebarPanel.add(dropdownLabel);
        sidebarPanel.add(dropdown);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                            PROGRESS BAR                                  ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Opret og tilføj progressBar til sidebar
        progressBar = new JProgressBar(0, 100); // Sørg for, at progressBar er en instansvariabel
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT); // Juster til venstre
        sidebarPanel.add(Box.createVerticalStrut(10)); // Tilføj lidt plads mellem dropdown og progressBar
        sidebarPanel.add(progressBar);

        // Tilføj sidebar til mainPanel
        mainPanel.add(sidebarPanel, BorderLayout.WEST);



        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                                TABLE                                     ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        // Opret ikke-redigerbar tabelmodel
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Gør alle celler ikke-redigerbare
            }
        };

        JTable table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false); // Forhindre kolonneflytning
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        // Indstil autoResizeMode
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Beregn kolonnebredden baseret på indholdet
        adjustColumnWidths(table);
        createClickableColumnHeaders(table, new Sorter());

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> {
            String input = inputField.getText().trim(); // Få tekst fra inputfelt
            if (!input.isEmpty()) {
                Sorter sorter = new Sorter();
                String[][][] results = sorter.searchCourses(input); // Kald searchCourses med input

                if (results != null) {
                    // Opdater tabelmodel med resultaterne
                    tableModel.setDataVector(results[1], unpackColumnName(results[0]));
                    adjustColumnWidths(table);
                } else {
                    JOptionPane.showMessageDialog(frame, "Ingen resultater fundet for søgetermen.", "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Indtast en søgetekst.", "Advarsel", JOptionPane.WARNING_MESSAGE);
            }
        });

        // ActionListener til dropdown
        dropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dropdown.getSelectedItem().equals("Placering")) {

                    // Kald din Sort-klasse for at hente placeringer
                    Sorter sorter = new Sorter();
                    String[][][] placements = sorter.getTable();

                    columnNames = unpackColumnName(placements[0]);
                    // System.out.println(columnNames);

                    tableModel.setDataVector(placements[1], columnNames);
                    adjustColumnWidths(table);
                }
            }
        });


        // ╔══════════════════════════════════════════════════════════════════════════╗
        // ║                          FINAL SETUP                                     ║
        // ╚══════════════════════════════════════════════════════════════════════════╝


        // Vis GUI
        frame.setVisible(true);
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
        // ║                 GET COLUMN MODEL AND SCREEN SIZE                         ║
        // ╚══════════════════════════════════════════════════════════════════════════╝

        TableColumnModel columnModel = table.getColumnModel();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (table.getRowCount() == 0) {

            // ╔══════════════════════════════════════════════════════════════════════╗
            // ║                  SET EQUAL WIDTH FOR ALL COLUMNS                     ║
            // ╚══════════════════════════════════════════════════════════════════════╝

            int equalWidth = (int) ((screenSize.getWidth() - 200) / table.getColumnCount());

            for (int col = 0; col < table.getColumnCount(); col++) {

                columnModel.getColumn(col).setPreferredWidth(equalWidth);

            }
        }

        else {


            // ╔══════════════════════════════════════════════════════════════════════╗
            // ║                  ADJUST WIDTH BASED ON CONTENT                       ║
            // ╚══════════════════════════════════════════════════════════════════════╝
            for (int col = 0; col < table.getColumnCount(); col++) {

                int width = 75; // Minimum bredde



                for (int row = 0; row < table.getRowCount(); row++) {

                    TableCellRenderer renderer = table.getCellRenderer(row, col);
                    Component comp = table.prepareRenderer(renderer, row, col);
                    width = Math.max(comp.getPreferredSize().width, width);

                }

                columnModel.getColumn(col).setPreferredWidth(width + 10); // Tilføj evt. padding

            }
        }
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

                // Hvis der klikkes på en ny kolonne, skal sorteringsretningen altid starte med
                // pil ned
                if (colIndex != lastSortedColumn) {
                    ascending = true;
                    lastSortedColumn = colIndex; // Opdater sidst sorterede kolonne
                }

                // Sortér data med `Sorter`-metoden
                String[][][] sortedData = sorter.sortTable(currentData, colIndex, ascending);

                // Opdater kolonnenavne med sorteringssymboler
                String[] tempColumnName = unpackColumnName(sortedData[0]);

                for (int i = 0; i < tempColumnName.length; i++) {

                    if (i == colIndex) {

                        tempColumnName[i] += ascending ? " ▼" : " ▲";

                    }

                    else {

                        tempColumnName[i] += " -"; // Nulstil andre kolonner

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


    public void updateProgress(int progress, String statusMessage) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            //statusLabel.setText(statusMessage);
        });
    }

}
