import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;



public class Sorter {

    ///////////////////////////////////////////////main (Metoder er udkommenteret)/////////////////////////////////////////////////
    public static void main(String[] args) {
        Sorter sorter = new Sorter();

        //String[][][] allData = sorter.getTable();
        //System.out.println(allData); 

        //String[][][] results = sorter.searchCourses("10036");
        //System.out.println(results);

        //String[][][] table = sorter.getTable();
        //table = sorter.sortTable(table, 0, true); // Den kan åbenbart ikke sortere ordenligt efter ECTS
        //sorter.printMatrix(table);
    }

    ////////////////////////////////////////////////////getTable()/////////////////////////////////////////////

        public String[][][] getTable() {
            String [][][] table = new String[2][][];

            try{
                List<HashMap<String, String>> data = getData();
                String[][] Matrix = new String[data.size()][6];
                String[][] columnNames = {{"Kursus nr."}, {"Kursusnavn"}, {"Skemaplacering"}, {"ECTS"}, {"Type"}, {"Institut"}};
                
                

                for (int i = 0; i < data.size(); i++) {
                    HashMap<String, String> course = data.get(i);
                    Matrix[i][0] = course.get("number"); //course_number
                    Matrix[i][1] = course.get("name"); //course_name
                    String placement = course.get("placement");
                    if (placement != null && placement.startsWith("-")) {
                        Matrix[i][2] = placement.substring(1).replace("-", ", "); //schedule_placement
                    }
                    else {
                        Matrix[i][2] = placement != null ? placement : ""; //schedule_placement
                    }
                    Matrix[i][3] = course.get("ECTS"); // ECTS
                    Matrix[i][4] = course.get("type"); // Type
                    Matrix[i][5] = course.get("institute"); // Institute
                }
                table[0] = columnNames;
                table[1] = Matrix;

                printMatrix(table); //Prints the table
                
            }
            catch (Exception ed) {
                ed.printStackTrace();
            }
            return table;
        }

    //////////////////////////////////////////////////////////sortTable()///////////////////////////////////////////////////
        
        public String[][][] sortTable(String[][][] table, int columnIndex, boolean ascending) {
            if (table == null || table.length <= 2 || table[1].length <= 1 || columnIndex < 0 || columnIndex >= table[1][0].length) {
                System.out.println("Invalid input");
                return table;
            }
            
            Arrays.sort(table[1], 1, table.length, new Comparator<String[]>() {
                @Override
                public int compare(String[] row1, String[] row2) {
                    if (columnIndex == 3) {
                        Double value1 = Double.parseDouble(row1[columnIndex].replace(",","."));
                        Double value2 = Double.parseDouble(row2[columnIndex].replace(",","."));
                        return ascending ? value1.compareTo(value2) : value2.compareTo(value1);
                    }
                    else {
                        return ascending ? row1[columnIndex].compareTo(row2[columnIndex]) : row2[columnIndex].compareTo(row1[columnIndex]);
                    }
                }
            });
            return table;
        }

    /////////////////////////////////////////////////////searchCourses(String searchTerm)///////////////////////////

        public String[][][] searchCourses(String searchTerm) {
            List<HashMap<String, String>> data = getData();
            String[][][] result = new String[2][][];

            String[][] coloumnnames = {{"Kursus nr."}, {"Kursusnavn"}, {"Skemaplacering"}, {"ECTS"}, {"Type"}, {"Institut"}};

            List<String[]> resultsList = new ArrayList<>();

            for (HashMap<String, String> course : data) {
                for (String value : course.values()){
                    if (value.toLowerCase().contains(searchTerm.toLowerCase())) {
                        String[] courseArray = {
                            course.get("number"), //course_number
                            course.get("name"), //course_name
                            placementHelper(course.get("placement")), //schedule_placement
                            course.get("ECTS"), // ECTS
                            course.get("type"), // Type
                            course.get("institute") // Institute
                        };
                        resultsList.add(courseArray);
                        break;
                    }
                }
            }

            if (resultsList.isEmpty()) { 
                System.out.println("No match found for searchterm");
                return null;
            }
            else {
                String[][] results = resultsList.toArray(new String[0][0]); 
                result[0] = coloumnnames;
                result[1] = results;
                printMatrix(result);
                return result;
            }
        }

        

    ////////////////////////////////////////HELPER FUNCTIONS//////////////////////////////////////////////////////

    //////////////////////////////////////////////printMatrix(String[][] Matrix)/////////////////////////

        public void printMatrix(String[][][] Matrix) {
            for (String[][] table : Matrix) {
                for (String[] row : table) {
                    System.out.println(String.join(", ", row));
                }
            }
        }

    ////////////////////////////////////////////placementHelper(String placement)////////////////////////////////////

        public String placementHelper(String placement) {
            if (placement != null && placement.startsWith("-")) {
                placement = placement.substring(1);
                placement = placement.replace("-", ", ");
            }
            else {
                placement = ""; //schedule_placement
            }
            return placement;
        }

    ////////////////////////////////////////////////////////getDATA()/////////////////////////////////////////////////
    
    public static List<HashMap<String, String>> getData() {
        Gson gson = new Gson();
        List<HashMap<String, String>> data = null;
        
        try (FileReader reader = new FileReader("courses.json")) {
            //PArse JSON til list of HashMaps
            Type listType = new TypeToken<List<HashMap<String, String>>>(){}.getType();
            data = gson.fromJson(reader, listType);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return data;
    } 
        


    ////////////////////////////////////////////OLD CODE AND NOTES/////////////////////////////////////////////////////////////////////

    //ArrayList<String> courseGrades = sorter.getGrades("Matematik 1a (Polyteknisk grundlag)");
    //System.out.println(courseGrades);

    /*public ArrayList<String> getGrades(String courseName) {
        Gson gson = new Gson();
        ArrayList<String> sortedArray = new ArrayList<String>();

        try (FileReader reader= new FileReader("Fake_data.json")) {
            //Parse JSON to list of HashMaps
            Type listType = new TypeToken<List<HashMap<String, String>>>(){}.getType();
            List<HashMap<String, String>> data = gson.fromJson(reader, listType);

            //Find the matching course, and add all the grades to the sortedArray
            for (HashMap<String, String> course : data) {  
                if (courseName.equals(course.get("course_name"))) {
                    String grades = course.get("grades");
                    grades = grades.replace(",", ".");

                    // Split grades til et array og tilføj til sortedArray
                    String[] gradeArray = grades.split(" ");
                    Collections.addAll(sortedArray, gradeArray);
                    break; //Exit the loop when match is found
                }
            }      
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return sortedArray;
    }*/

        /*
        Tilføj flere metoder herunder:
            - En metode, der kan finde karaktererne for et kursus
            - En metode, der kan finde alle kurser, der har en bestemt underviser / give en liste over alle undervisere til et 
              bestemt kursus
            - En metode til arbejdstid

        */

        /*////////////////////////////////////////////////////getSchedulePlacements()////////////////////////////////////////////////////

    public ArrayList<String> getSchedulePlacements(){
        ArrayList<String> sortedArray = new ArrayList<String>();
        List<HashMap<String, String>> data = getData();

        try { 
            //Extract the schedulePlacement from each HashMap and add it to the sortedArray
            for (HashMap<String, String> course : data) {
                String placement = course.get("schedule_placement");
                if (placement != null) { //skal det bruges???
                    sortedArray.add(placement);
                }
            }  
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return sortedArray; 
    }*/

    /*///////////////////////////////////////////////////////getAll(String courseName)/////////////////////////////////

    public ArrayList<String> getAll(String courseName) {
        ArrayList<String> sortedArray = new ArrayList<String>();
        List<HashMap<String, String>> data = getData();

        try { 
            //Find the matching course, and add all the details to the sortedArray
            for  (HashMap<String, String> course : data) {
                if (courseName.equals(course.get("course_name"))) {
                    sortedArray.addAll(course.values());
                    break; //Exit the loop when match is found
                }
            } 
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return sortedArray; 
    }*/

    /*///////////////////////////////////getCourseWithPlacement(String)////////////////////////
        public ArrayList<String> search(String query) {
            ArrayList<String> list = new ArrayList<>();
            List<HashMap<String, String>> data = getData();

            for  (HashMap<String, String> course : data) {
                if (query.toLowerCase().equals(course.get("placement").toLowerCase())) { //Har ændret til toLowerCase()                    
                    list.add(course.get("name"));
                }
            }

            return list;
        }*/

        //Alt dette er til at kalde de gamle metoder
        //ArrayList<String> courseWithPlacement = sorter.search("F3B");
        //System.out.println(courseWithPlacement);

        //ArrayList<String> schedulePlacements = sorter.getSchedulePlacements();
        //System.out.println(schedulePlacements);

        //ArrayList<String> courseDetails = sorter.getAll("Matematik 1a (Polyteknisk grundlag)");
        //System.out.println(courseDetails); 
}
