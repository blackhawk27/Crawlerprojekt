///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                           //
//                                                      IMPORTS                                                              //
//                                                                                                                           //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                           //
//                                                      CLASS SORTER                                                         //
//                                                                                                                           //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



public class Sorter {



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                        //
    //                                                   MAIN FUNCTIONS                                                       // 
    //                                                                                                                        //    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    

    //Uncomment the main function to test the code. 
    //Remember to uncomment the printMatrix() method in the "Helperfunctions" section, and in the method you want
    //to test to see the output. 

    public static void main(String[] args) {
        Sorter sorter = new Sorter();

        //String[][][] allData = sorter.getTable();
        //System.out.println(allData); 

        //String[][][] results = sorter.searchCourses("10060");
        //System.out.println(results);

        //String[][][] table = sorter.getTable();
        //table = sorter.sortTable(table[1], 0, true); // Den kan åbenbart ikke sortere ordenligt efter ECTS
        //sorter.printMatrix(table);

        //String[][][] filteredSearch = sorter.filterSearch("F4B", "matematik", "diplom");
        //System.out.println(filteredSearch);
    }

    ////////////////////////////////////////////////////getTable()/////////////////////////////////////////////

        public String[][][] getTable() {
            String [][][] table = new String[2][][];

            try{
                List<HashMap<String, String>> data = getData();                                                 //Get the data from the json file
                String[][] Matrix = new String[data.size()][6];
                String[][] columnNames = {{"Kursus nr."}, {"Kursusnavn"}, {"Skemaplacering"}, {"ECTS"}, {"Type"}, {"Institut"}};
                
                for (int i = 0; i < data.size(); i++) {                                                         //Iterate through the data and add it to the matrix
                    HashMap<String, String> course = data.get(i);                                               //Get the course
                    Matrix[i][0] = course.get("number");                                                    //course_number
                    Matrix[i][1] = course.get("name");                                                      //course_name
                    String placement = course.get("placement");
                    if (placement != null && placement.startsWith("-")) {
                        Matrix[i][2] = placement.substring(1).replace("-", ", "); //schedule_placement
                    }
                    else {
                        Matrix[i][2] = placement != null ? placement : "";                                      //schedule_placement
                    }
                    Matrix[i][3] = course.get("ECTS");                                                      // ECTS
                    Matrix[i][4] = course.get("type");                                                      // Type
                    Matrix[i][5] = course.get("institute");                                                 // Institute
                }
                table[0] = columnNames;
                table[1] = Matrix;

                //printMatrix(table); 
            }
            catch (Exception ed) {                                                                              //Catch exceptions
                ed.printStackTrace();
            }
            return table;
        }

    //////////////////////////////////////////////////////////sortTable()///////////////////////////////////////////////////
        
        public String[][][] sortTable(String[][] table, int columnIndex, boolean ascending) {
            String[][][] result = new String[2][][];
            String[][] coloumnnames = {{"Kursus nr."}, {"Kursusnavn"}, {"Skemaplacering"}, {"ECTS"}, {"Type"}, {"Institut"}};

            if (table == null || table.length <= 1 || columnIndex < 0 || columnIndex >= table[0].length) {       //Check if the input is valid
                System.out.println("Invalid input");
                return null;
            }
            
            Arrays.sort(table, 0, table.length, new Comparator<String[]>() {                            //Sort the table
                @Override                                                                                         //Override the compare method
                public int compare(String[] row1, String[] row2) {                                                //Compare the rows
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
            result[0] = coloumnnames;                                                                             // Asemble the result
            result[1] = table;
            //printMatrix(result);
            return result;
        }   

    /////////////////////////////////////////////////////searchCourses(String)///////////////////////////////////////////////

        public String[][][] searchCourses(String searchTerm) {
            List<HashMap<String, String>> data = getData();                         //Get the data from the json file
            String[][][] result = new String[2][][];
            String[][] coloumnnames = {{"Kursus nr."}, {"Kursusnavn"}, {"Skemaplacering"}, {"ECTS"}, {"Type"}, {"Institut"}};
            List<String[]> resultsList = new ArrayList<>();

            for (HashMap<String, String> course : data) {                           //Iterate through the data
                for (String value : course.values()){
                    if (value.toLowerCase().contains(searchTerm.toLowerCase())) {
                        String[] courseArray = {
                            course.get("number"),                               //course_number
                            course.get("name"),                                 //course_name
                            placementHelper(course.get("placement")),           //schedule_placement
                            course.get("ECTS"),                                 // ECTS
                            course.get("type"),                                 // Type
                            course.get("institute")                             // Institute
                        };
                        resultsList.add(courseArray);
                        break;
                    }
                }
            }

            if (resultsList.isEmpty()) {                                            //Check if the list is empty
                System.out.println("No match found for searchterm");
                return null;
            }
            else {
                String[][] results = resultsList.toArray(new String[0][0]); 
                result[0] = coloumnnames;                                           // Asemble the result
                result[1] = results;
                //printMatrix(result);
                return result;
            }
        }

    ////////////////////////////////////////filterSearch(String, String, String)////////////////////////////////////////////////
    
    public String[][][] filterSearch(String placement, String institute, String type) {
        List<HashMap<String, String>> data = getData();                 // get the data from the json file
        String[][][] result = new String[2][][];
        String[][] coloumnnames = {{"Kursus nr."}, {"Kursusnavn"}, {"Skemaplacering"}, {"ECTS"}, {"Type"}, {"Institut"}};
        List<String[]> resultsList = new ArrayList<>();

        if (placement == null && institute == null && type == null) {   //Check if the input is valid
            System.out.println("Invalid input");
            return null;
        }

        for (HashMap<String, String> course : data) {
            if (matchesCriteria(course, placement, institute, type)) {  //Check if the course matches the criteria
                    String[] courseArray = {
                        course.get("number"),                       //course_number
                        course.get("name"),                         //course_name
                        placementHelper(course.get("placement")),   //schedule_placement
                        course.get("ECTS"),                         // ECTS
                        course.get("type"),                         // Type
                        course.get("institute")                     // Institute
                    };
                    resultsList.add(courseArray);
                }
        }

        if (resultsList.isEmpty()) {                                    //Check if the list is empty
            System.out.println("No match found for searchterm");
            return null;
        }
        else {
            String[][] results = resultsList.toArray(new String[0][0]); 
            result[0] = coloumnnames;                                   // Asemble the result
            result[1] = results;
            //printMatrix(result);
            return result;
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                             HELPER FUNCTIONS                                                   //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    


    //////////////////////////////////////////////printMatrix(String[][] Matrix)/////////////////////////

        /*
        private void printMatrix(String[][][] Matrix) { //Helper function to print the matrix (internal use)
            for (String[][] table : Matrix) {
                for (String[] row : table) {
                    System.out.println(String.join(", ", row));
                }
            }
        }
        */

    ////////////////////////////////////////////placementHelper(String placement)////////////////////////////////////

        private String placementHelper(String placement) {                  //Helper function to format the placement 
            if (placement != null && placement.startsWith("-")) {    //Check if the placement starts with a "-"
                placement = placement.substring(1); 
                placement = placement.replace("-", ", "); 
            }
            else {
                placement = ""; 
            }
            return placement;
        }

    ////////////////////////////////////////////////////////getDATA()/////////////////////////////////////////////////
    
    private static List<HashMap<String, String>> getData() {                             //Helper function to get the data from the json file
        Gson gson = new Gson();
        List<HashMap<String, String>> data = null;
        
        try (FileReader reader = new FileReader("courses.json")) {              //Read the json file
            //PArse JSON til list of HashMaps
            Type listType = new TypeToken<List<HashMap<String, String>>>(){}.getType();  //Create a list of hashmaps
            data = gson.fromJson(reader, listType);                                      //Parse the data
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return data;
    } 

    ////////////////////////////////////////machtesCrieria(HashMap, String, String, String)/////////////////////////////////////
    
    private boolean matchesCriteria(HashMap<String, String> course, String placement, String institute, String type) { //Helper function to check if the course matches the criteria
        boolean matches = true;
        if (placement != null) {
            matches = matches && course.get("placement").toLowerCase().contains(placement.toLowerCase());           //Check if the placement matches
        } 
        if (institute != null) {
            matches = matches && course.get("institute").toLowerCase().contains(institute.toLowerCase());           //Check if the institute matches
        }
        if (type != null) {
            matches = matches && course.get("type").toLowerCase().contains(type.toLowerCase());                     //Check if the type matches
        }
        return matches;
    }
}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                           //
//                                                     OLD CODE AND NOTES                                                    //
//                                                                                                                           //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//Removed from this version of the code