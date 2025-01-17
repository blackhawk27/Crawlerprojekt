
public class Sorter_Test {

    public String[][] getCoursesByPlacement() {
        // Hardkodet JSON-data fra spørgsmålet
        String[][] placement = {
                { "01001", "Matematik 1a (Polyteknisk grundlag)", "E1A", "10",
                        "01 Institut for Matematik og Computer Science" },
                { "01002", "Matematik 1b (polyteknisk grundlag)", "F1A", "10",
                        "01 Institut for Matematik og Computer Science" },
                { "01003", "Matematik 1a (Polyteknisk grundlag)", "E3B", "10",
                        "01 Institut for Matematik og Computer Science" },
                { "01004", "Matematik 1b: (Polyteknisk grundlag)", "F3B", "10",
                        "01 Institut for Matematik og Computer Science" }
        };

        return placement; // Returner det udfyldte 2D-array
    }

    public static void main(String[] args) {
        Sorter_Test sorter = new Sorter_Test();
        String[][] courses = sorter.getCoursesByPlacement();

        // Udskriv arrayet for at kontrollere output
        for (String[] course : courses) {
            for (String detail : course) {
                System.out.print(detail);
            }
            System.out.println();
        }
    }
}
