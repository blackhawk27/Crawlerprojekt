import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;




public class Sorter {
    public static void main(String[] args) {
        Sorter sorter = new Sorter();

        ArrayList<String> schedulePlacements = sorter.getSchedulePlacements();
        System.out.println(schedulePlacements);

        ArrayList<String> courseDetails = sorter.getAll("Matematik 1a (Polyteknisk grundlag)");
        System.out.println(courseDetails);
    }

        public ArrayList<String> getSchedulePlacements(){
            Gson gson = new Gson(); // Makes a new Gson object
            ArrayList<String> sortedArray = new ArrayList<String>();

            try (FileReader reader = new FileReader("Fake_data.json")) { 
                //Parse JSON to list of HashMaps
                Type listType = new TypeToken<List<HashMap<String, String>>>(){}.getType(); 
                List<HashMap<String, String>> data = gson.fromJson(reader, listType); 

                //Extract the schedulePlacement from each HashMap and add it to the sortedArray
                for (HashMap<String, String> course : data) {
                    String placement = course.get("schedule_placement");
                    if (placement != null) { //skal det bruges???
                        sortedArray.add(placement);
                    }
                }  
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return sortedArray; 
        }

        public ArrayList<String> getAll(String courseName) {
            Gson gson = new Gson(); // Makes a new Gson object
            ArrayList<String> sortedArray = new ArrayList<String>();

            try (FileReader reader = new FileReader("Fake_data.json")) { 
                //Parse JSON to list of HashMaps
                Type listType = new TypeToken<List<HashMap<String, String>>>(){}.getType(); 
                List<HashMap<String, String>> data = gson.fromJson(reader, listType); 

                //Find the matching course, and add all the details to the sortedArray
                for  (HashMap<String, String> course : data) {
                    if (courseName.equals(course.get("course_name"))) {
                        sortedArray.addAll(course.values());
                        break; //Exit the loop when match is found
                    }
                } 
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return sortedArray; 
        }

        /*
        Tilføj flere metoder herunder:
            - En metode, der kan finde karaktererne for et kursus
            - En metode, der kan finde alle kurser, der har en bestemt underviser / give en liste over alle undervisere til et 
              bestemt kursus
            - En metode til arbejdstid
            
        */
    
    
}
