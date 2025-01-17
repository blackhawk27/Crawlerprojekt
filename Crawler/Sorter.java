import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.HashMap;
import java.util.List;




public class Sorter {

    ///////////////////////////////////////////////main (Funktioner er udkommenteret)/////////////////////////////////////////////////
    public static void main(String[] args) {
        Sorter sorter = new Sorter();

        //String[][] allData = sorter.getTable();
        //System.out.println(allData); 

        //String[][] results = sorter.searchCourses("03");
        //System.out.println(results);

        ArrayList<String> courseWithPlacement = sorter.getCourseWithPlacement("E1A");
        System.out.println(courseWithPlacement);

        //ArrayList<String> schedulePlacements = sorter.getSchedulePlacements();
        //System.out.println(schedulePlacements);

        //ArrayList<String> courseDetails = sorter.getAll("Matematik 1a (Polyteknisk grundlag)");
        //System.out.println(courseDetails);
    }

    ////////////////////////////////////////////////////////getDATA()/////////////////////////////////////////////////
    
    public static List<HashMap<String, String>> getData() {
        Gson gson = new Gson();
        List<HashMap<String, String>> data = null;
        
        try (FileReader reader = new FileReader("Fake_data.json")) {
            //PArse JSON til list of HashMaps
            Type listType = new TypeToken<List<HashMap<String, String>>>(){}.getType();
            data = gson.fromJson(reader, listType);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return data;
    } 

////////////////////////////////////////////////////getSchedulePlacements()////////////////////////////////////////////////////

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
    }

    ////////////////////////////////////////////////////////getAll(String courseName)/////////////////////////////////

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
    }

    ////////////////////////////////////////////////////getTable()/////////////////////////////////////////////

        public String[][] getTable() {
            String [][] Matrix = null;

            try{
                List<HashMap<String, String>> data = getData();
                Matrix = new String[data.size() + 1][5];
                
                Matrix[0][0] = "Coursenumber";
                Matrix[0][1] = "Coursename";
                Matrix[0][2] = "Schedule placement";
                Matrix[0][3] = "ECTS";
                Matrix[0][4] = "Institute";

                for (int i = 0; i < data.size(); i++) {
                    HashMap<String, String> course = data.get(i);
                    Matrix[i + 1][0] = course.get("course_number"); //course_number
                    Matrix[i + 1][1] = course.get("course_name"); //course_name
                    Matrix[i + 1][2] = course.get("schedule_placement"); //schedule_placement
                    Matrix[i + 1][3] = course.get("ECTS"); // ECTS
                    Matrix[i + 1][4] = course.get("Institute"); // Institute
                }

                printMatrix(Matrix); //noget med typerne er galt 
                
            }
            catch (Exception ed) {
                ed.printStackTrace();
            }
            return Matrix;
        }

        /////////////////////////////////////////////////////searchCourses(String searchTerm)///////////////////////////

        public String[][] searchCourses(String searchTerm) {
            List<HashMap<String, String>> data = getData();
            List<String[]> results = new ArrayList<>();
            String[][] result = null;

            results.add(new String[]{"Coursenumber", "Coursename", "Scheduleplacement", "ECTS", "Institue"});

            for (HashMap<String, String> course : data) {
                for (String value : course.values()){
                    if (value.toLowerCase().contains(searchTerm.toLowerCase())) {
                        String[] courseArray = {
                            course.get("course_number"), //course_number
                            course.get("course_name"), //course_name
                            course.get("schedule_placement"), //schedule_placement
                            course.get("ECTS"), // ECTS
                            course.get("Institute") // Institute
                        };
                        results.add(courseArray);
                        break;
                    }
                }
            }

            if (results.size() == 1) {
                System.out.println("No match found for searchterm");
                return null;
            }
            else {
                result = results.toArray(new String[0][0]);
                printMatrix(result);
                return result;
            }
        }

        ////////////////////////////////////getCourseWithPlacement(String)////////////////////////
        public ArrayList<String> getCourseWithPlacement(String schedulePlacement) {
            ArrayList<String> placements = new ArrayList<>();
            List<HashMap<String, String>> data = getData();

            for  (HashMap<String, String> course : data) {
                if (schedulePlacement.equals(course.get("schedule_placement"))) {
                    placements.add(course.get("course_name"));
                }
            }

            return placements;
        }

        ////////////////////////////////////////HELPER FUNCTIONS//////////////////////////////////////////////////////

        //////////////////////////////////////////////printMatrix(String[][] Matrix)/////////////////////////

        public static void printMatrix(String[][] Matrix) {
            for (String[] row : Matrix) {
                System.out.println(String.join(", ", row));
            }

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


        /* Sådan læses fra en JSON-fil. Alle metoder skal startes sådan
         Gson gson = new Gson(); // Makes a new Gson object
            ArrayList<String> sortedArray = new ArrayList<String>();

            try (FileReader reader = new FileReader("Fake_data.json")) { 
                //Parse JSON to list of HashMaps
                Type listType = new TypeToken<List<HashMap<String, String>>>(){}.getType(); 
                List<HashMap<String, String>> data = gson.fromJson(reader, listType); 
            
                ***Resten af koden er***
            }

            catch (IOException e) {
                e.printStackTrace();
            }
         */
    
    
}
