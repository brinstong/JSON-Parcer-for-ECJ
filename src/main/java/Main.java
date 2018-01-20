import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

public class Main {

    final static String NODE_DATA = "NodeData";


    public static void main(String[] args)throws IOException{
/*

        File inputFile = new File("highdimension.params");
        File outputFile = new File("highdimension.json");
        String json = paramsToJson(inputFile, outputFile).toString();
        equalityCheck(outputFile, inputFile);
*/


/*

        File inputFile = new File("highdimension.json");
        File outputFile = new File("highdimension.params");
        String json = JsonToParams(inputFile, outputFile).toString();
        equalityCheck(inputFile, outputFile);

*/

    }

    public static boolean equalityCheck(File jsonFile, File paramsFile) throws IOException {

        JsonObject json = new JsonParser().parse(new FileReader(jsonFile)).getAsJsonObject();
        Properties properties = new Properties();
        properties.load(new FileReader(paramsFile));

        for (Object key : properties.keySet()) {

            //System.out.println(key);
            String correctValue = properties.getProperty((String)key);
            //System.out.println(correctValue);

            LinkedList<String> subKeyList = new LinkedList<>(Arrays.asList(((String) key).split("\\.")));

            if (!checkInJson(json, subKeyList, correctValue)) {
                System.err.println("Equality Check Failed !! for key "+key);
                return false;
            }


        }

        System.out.println("Equality Check passed !!");
        return true;
    }

    private static boolean checkInJson(JsonObject json,LinkedList<String>  keys, String correctValue) {


        JsonElement subJson = json.deepCopy();

        for (int i = 0; i < keys.size()-1; i++) {
            if (subJson.isJsonObject()) {
                if (subJson.getAsJsonObject().has(keys.get(i))) {
                    subJson = subJson.getAsJsonObject().get(keys.get(i));
                }
                else return false;
            }
            else if (subJson.isJsonArray()) {
                int location;
                try {
                    location = Integer.parseInt(keys.get(i));

                }catch (NumberFormatException e) {
                    return false;
                }

                if (subJson.getAsJsonArray().size()>location) {
                    subJson = subJson.getAsJsonArray().get(location);
                }
                else return false;
            }
        }

        if (keys.getLast().equals("size")) {
            return true;
        }

        if (subJson.isJsonObject()) {
            String lastKey = keys.getLast();
            String valueInJson = subJson.getAsJsonObject().get(lastKey).getAsString();
            if (subJson.getAsJsonObject().get(keys.getLast()).getAsString().trim().equals(correctValue)) {
                return true;
            }
        }
        else if (subJson.isJsonArray()) {
            if (subJson.getAsJsonArray().get(Integer.parseInt(keys.getLast())).getAsJsonObject().get(NODE_DATA).getAsString().equals(correctValue)) {
                return true;
            }
        }
        else {
            System.out.println(subJson);
        }



        return false;
    }


    public static String JsonToParams(File json, File outputFile) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(json));
        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(br).getAsJsonObject();

        return  JsonToParams(jsonObject, outputFile);
    }

    public static String JsonToParams(JsonObject json, File outputFile) throws IOException {
        StringBuilder params = new StringBuilder();

        Properties properties = new Properties();
        OutputStream outputStream = new FileOutputStream(outputFile);

        //System.out.println(json.toString());

        HashMap<String, String> keyVals = new HashMap<>();
        dissolveHeirarchy(json, keyVals, "");


        for (String key : keyVals.keySet()) {
            //System.out.println(key+"\t\t\t\t\t\t\t\t\t\t\t\t"+keyVals.get(key));

            params.append(key+"\t=\t"+keyVals.get(key)+"\n");

            properties.setProperty(key,keyVals.get(key));

        }

        properties.store(outputStream,null);
        if (outputStream != null) {
            outputStream.close();
        }

        return params.toString();
    }

    private static void dissolveHeirarchy(JsonElement currentRoot ,HashMap<String, String> keyVals, String heirarchy) {


        String currentHeirarchy = heirarchy;

        if (currentRoot.isJsonObject()) {

            for (String key : currentRoot.getAsJsonObject().keySet()) {

                heirarchy = currentHeirarchy;

                heirarchy += key + ".";

                dissolveHeirarchy(currentRoot.getAsJsonObject().get(key),keyVals,heirarchy);
            }

        }
        else if (currentRoot.isJsonArray()) {

            int size = currentRoot.getAsJsonArray().size();
            keyVals.put(heirarchy+"size",""+size);

            for (int i = 0; i < size; i++) {
                heirarchy = currentHeirarchy;
                heirarchy += i + ".";

                dissolveHeirarchy(currentRoot.getAsJsonArray().get(i),keyVals,heirarchy);
            }

        }
        else {

            if (heirarchy.endsWith(NODE_DATA+".")) {
                System.out.println(heirarchy.substring(0,heirarchy.length()-(NODE_DATA.length()+2))+ " : "+ currentRoot.getAsString().trim());
                keyVals.put(heirarchy.substring(0,heirarchy.length()-(NODE_DATA.length()+1)) ,  currentRoot.getAsString().trim());
            }
            else {
                System.out.println(heirarchy.substring(0,heirarchy.length()-1)+ " : "+ currentRoot.getAsString().trim());
                keyVals.put(heirarchy.substring(0,heirarchy.length()-1), currentRoot.getAsString().trim());
            }

        }

    }






    public static JsonObject paramsToJson(File paramsFile, File outputFile) throws IOException {
        HashMap<LinkedList<String>, String> keyVals = new HashMap<>();

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(paramsFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            LinkedList<String> sub_keys = new LinkedList<>(Arrays.asList(key.split("\\.")));
            keyVals.put(sub_keys, value);
//            System.out.println(key + " => " + keyVals.get(key));
        }

        JsonObject jsonObject = generateJSON(keyVals);

        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(outputFile);

        fileWriter.write(jsonObject.toString());

        fileWriter.close();

        return jsonObject;
    }


    private static JsonObject generateJSON(HashMap<LinkedList<String>, String> keyVals) {

        JsonObject root = new JsonObject();

        // Generate Structure

        int longest_key_length = 0;

        LinkedList<LinkedList<String>> keysWithSize = new LinkedList<>();

        for (LinkedList<String> keys : keyVals.keySet()) {


            if (keys.contains("size")) {

                keysWithSize.add(keys);
                if (keys.size() > longest_key_length) {
                    longest_key_length = keys.size();
                }


            }
        }

        for (int i = 1; i <= longest_key_length; i++) {


            for (LinkedList<String> keys : keysWithSize) {
                if (keys.size() == i) {
                    System.out.println("Key : " + Arrays.toString(keys.toArray()));
                    System.out.println("Val : " + keyVals.get(keys));
                    try {
                        generateHeirarchy(root, keys, keyVals.get(keys));
                    } catch (NumberFormatException e) {
                        System.err.println("Could not add "+keys);
                        System.err.println("Index not present while inserting into JsonArray. The file is inconsistent");
                    }
                    System.out.println("Root : " + root);
                }

            }

        }





        for (LinkedList<String> keys : keyVals.keySet()) {

            if (!keys.contains("size")) {
                System.out.println("Key : " + Arrays.toString(keys.toArray()));
                System.out.println("Val : " + keyVals.get(keys));
                try {
                    generateHeirarchy(root, keys, keyVals.get(keys));
                } catch (NumberFormatException e) {
                    System.err.println("Could not add "+keys+"\t"+"Index not present while inserting into JsonArray. The file is inconsistent");

                }
                System.out.println("Root : " + root);
            }
        }





        return root;
    }

    private static void generateHeirarchy(JsonElement root, LinkedList<String> keys, String val) throws NumberFormatException{

        //System.out.println("Root now : "+root);


        if (keys.size() == 0) {
            JsonObject root_ref = root.getAsJsonObject();
            root_ref.addProperty(NODE_DATA, val);
        } else if (keys.size() == 1) {
            if (root.isJsonObject()) {
                JsonObject root_ref = root.getAsJsonObject();
                root_ref.addProperty(keys.getFirst(), val);
            } else if (root.isJsonArray()) {
                JsonArray root_ref = root.getAsJsonArray();
                JsonObject leaf_node = new JsonObject();
                leaf_node.addProperty(NODE_DATA, val);
                int location = Integer.parseInt(keys.getFirst());
                root_ref.set(location, leaf_node);
            }
        } else if (keys.size() == 2 && keys.get(1).equals("size")) {


            String key = keys.get(0);
            // create the heirarchy for key (JsonArray) here and initialize the array to size equal to val
            int size = Integer.parseInt(val);
            JsonArray jsonArray = new JsonArray();


            while (jsonArray.size() < size) {
                jsonArray.add(new JsonObject());
            }


            if (root.isJsonArray()) {
                int location = Integer.parseInt(key);
                JsonArray root_ref = root.getAsJsonArray();

                if (!root_ref.get(location).isJsonArray()) {
                    root_ref.set(location, jsonArray);
                }
            } else if (root.isJsonObject()) {
                JsonObject root_ref = root.getAsJsonObject();
                if (!root_ref.has(key)) {
                    root_ref.add(key, jsonArray);
                }
            }

        } else if (keys.size() >= 2) {
            // create one heirarchy and recursively call on the remaining keys
            String key1 = keys.get(0);
            String key2 = keys.get(1);




            keys.removeFirst();


            if (root.isJsonObject()) {

                try {
                    Integer.parseInt(key1);
                    System.err.println("The parent of "+root+" should probably be a defined as a JsonArray");
                }catch (NumberFormatException e) {

                }

                JsonObject root_ref = root.getAsJsonObject();

                if (!root_ref.has(key1)) {
                    root_ref.add(key1, new JsonObject());
                }
                generateHeirarchy(root_ref.get(key1), keys, val);

            } else if (root.isJsonArray()) {
                int location = Integer.parseInt(key1);

                JsonArray root_ref = root.getAsJsonArray();

                generateHeirarchy(root_ref.get(location), keys, val);
            }
        }

    }



}

