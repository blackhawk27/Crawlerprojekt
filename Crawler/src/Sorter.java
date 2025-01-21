///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                      IMPORTS                                                              //
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
//                                                      CLASS SORTER                                                         //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class Sorter {



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                   MAIN FUNCTION                                                        //     
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    


    public static void main(String[] args) {
        Sorter sorter = new Sorter();

        //String[][][] allData = sorter.getTable();
        //System.out.println(allData); 

        //String[][][] results = sorter.searchCourses("10036");
        //System.out.println(results);

        //String[][][] table = sorter.getTable();
        //table = sorter.sortTable(table[1], 0, true); // Den kan åbenbart ikke sortere ordenligt efter ECTS
        //sorter.printMatrix(table);

        //String[][][] courseWithPlacement = sorter.searchCourseWithPlacement("E1A");
        //System.out.println(courseWithPlacement);

        //String[][][] courseWithInstitute = sorter.searchCourseWithInstitute("01");
        //System.out.println(courseWithInstitute);
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

                //printMatrix(table); //Prints the table
                
            }
            catch (Exception ed) {
                ed.printStackTrace();
            }
            return table;
        }

    //////////////////////////////////////////////////////////sortTable()///////////////////////////////////////////////////
        
        public String[][][] sortTable(String[][] table, int columnIndex, boolean ascending) {
            String[][][] result = new String[2][][];
            String[][] coloumnnames = {{"Kursus nr."}, {"Kursusnavn"}, {"Skemaplacering"}, {"ECTS"}, {"Type"}, {"Institut"}};

            if (table == null || table.length <= 1 || columnIndex < 0 || columnIndex >= table[0].length) {
                System.out.println("Invalid input");
                return null;
            }
            
            Arrays.sort(table, 0, table.length, new Comparator<String[]>() {
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
            result[0] = coloumnnames;
            result[1] = table;
            //printMatrix(result);
            return result;
        }

    /////////////////////////////////////////////////////searchCourses(String)///////////////////////////

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
                //printMatrix(result);
                return result;
            }
        }

    //////////////////////////////////searchCourseWithPlacement(String)////////////////////////////////////////////////
        
        public String[][][] searchCourseWithPlacement(String query) {
            List<HashMap<String, String>> data = getData();
            String[][][] result = new String[2][][];
            String[][] coloumnnames = {{"Kursus nr."}, {"Kursusnavn"}, {"Skemaplacering"}, {"ECTS"}, {"Type"}, {"Institut"}};
            List<String[]> resultList = new ArrayList<>();
            
            

            for (HashMap<String, String> course : data) {
                if (course.get("placement").toLowerCase().contains(query.toLowerCase())) {
                        String[] courseArray = {
                            course.get("number"), //course_number
                            course.get("name"), //course_name
                            placementHelper(course.get("placement")), //schedule_placement
                            course.get("ECTS"), // ECTS
                            course.get("type"), // Type
                            course.get("institute") // Institute
                        };
                        resultList.add(courseArray);
                    }
            }

            String[][] results = resultList.toArray(new String[0][0]); 
            result[0] = coloumnnames;
            result[1] = results;
            //printMatrix(result);
            return result;
        }

        //////////////////////////////////searchCourseWithInstitute(String)////////////////////////////////////////////////
        
        public String[][][] searchCourseWithInstitute(String query) {
            List<HashMap<String, String>> data = getData();
            String[][][] result = new String[2][][];
            String[][] coloumnnames = {{"Kursus nr."}, {"Kursusnavn"}, {"Skemaplacering"}, {"ECTS"}, {"Type"}, {"Institut"}};
            List<String[]> resultList = new ArrayList<>();
            
            

            for (HashMap<String, String> course : data) {
                if (course.get("institute").toLowerCase().contains(query.toLowerCase())) {
                        String[] courseArray = {
                            course.get("number"), //course_number
                            course.get("name"), //course_name
                            placementHelper(course.get("placement")), //schedule_placement
                            course.get("ECTS"), // ECTS
                            course.get("type"), // Type
                            course.get("institute") // Institute
                        };
                        resultList.add(courseArray);
                    }
            }

            String[][] results = resultList.toArray(new String[0][0]); 
            result[0] = coloumnnames;
            result[1] = results;
            //printMatrix(result);
            return result;
        }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                             HELPER FUNCTIONS                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    


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

}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                     OLD CODE AND NOTES                                                    //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//Removed from this version of the code

