import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

public class Main {

/*
    public static void main(String[] args)throws IOException{
        File inputFile = new File("highdimension.params");
        File outputFile = new File("highdimension.json");
        String json = paramsToJson(inputFile, outputFile).toString();
        equalityCheck(outputFile, inputFile);


        File inputFile1 = new File("simple.json");
        File outputFile1 = new File("temp.params");
        String params = JsonToParams(inputFile1, outputFile1);
        equalityCheck(inputFile1, outputFile1);





    }
*/

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
                System.err.println("Equality Check Failed !!");
                return false;
            }


        }

        System.out.println("Equality Check passed !!");
        return true;
    }

    private static boolean checkInJson(JsonObject json,LinkedList<String>  keys, String correctValue) {

        final String NODE_DATA = "NodeData";


        for (int i=0; i<keys.size()-1; i++) {

            if (json.has(keys.get(i)) && json.get(keys.get(i)) instanceof JsonObject) {

                json = json.getAsJsonObject(keys.get(i));

            }
            else {
                return false;
            }


        }

        if (json.has(NODE_DATA)) {
            json = json.getAsJsonObject(NODE_DATA);
        }
        else return false;


        if (json.has(keys.getLast()) && json.get(keys.getLast()).getAsString().equals(correctValue))
        {
            return true;
        }
        else return false;

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

    private static void dissolveHeirarchy(JsonObject currentRoot ,HashMap<String, String> keyVals, String heirarchy) {

        final String NODE_DATA = "NodeData";
        String currentHeirarchy = heirarchy;



        for (String key : currentRoot.keySet()) {

            //System.out.println(key);

            heirarchy = currentHeirarchy;

            if (key.equals(NODE_DATA)) {

                addAllLeafNodes(keyVals, currentRoot.getAsJsonObject(NODE_DATA),currentHeirarchy);

            }
            else {
                heirarchy += key+".";
                dissolveHeirarchy(currentRoot.getAsJsonObject(key), keyVals, heirarchy);
            }
        }


    }


    private static void addAllLeafNodes(HashMap<String, String> keyVals, JsonObject nodes, String heirarchy) {

        for (String key : nodes.keySet()) {

            String val = nodes.get(key).toString();

            // to remove quotes around the val
            val = val.substring(1,val.length()-1);

            keyVals.put(heirarchy+key, val);

        }

    }


    public static JsonObject paramsToJson(File paramsFile, File outputFile) throws IOException {
        HashMap<String, String> keyVals = new HashMap<>();

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(paramsFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            keyVals.put(key,value);
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

    private static JsonObject generateJSON(HashMap<String, String> keyVals) {

        JsonObject root = new JsonObject();

        for (String key : keyVals.keySet()) {

            LinkedList<String> sub_keys = new LinkedList<>(Arrays.asList(key.split("\\.")));

            addToJson(root, sub_keys, keyVals.get(key));

            //System.out.println(root);
        }
        return root;
    }

    private static void addToJson(JsonObject root, LinkedList<String> sub_keys, String val) {

        /*
        We create a JsonObject of name NODE_DATA to store any leaf nodes.
        The reason being that, sometimes the heirarchy names are same as that of leaf nodes, so we cannot have such in Json.

        Like these in simple params :
        pop.subpop.0 => ec.Subpopulation
        pop.subpop.0.duplicate-retries => 0
         */
        final String NODE_DATA = "NodeData";


        //System.out.println(root);
        //System.out.println("Sub-key : "+sub_keys);
        //System.out.println("Val : "+val);

        if (sub_keys.size() == 0) {
            return;
        }
        else if (sub_keys.size() == 1) {
            // property will be replaced by newer one
            if (!root.has(NODE_DATA)) {
                root.add(NODE_DATA,new JsonObject());
            }
            root.get(NODE_DATA).getAsJsonObject().addProperty(sub_keys.get(0), val);
        }
        else {
            // handling heirarchy here

            String key = sub_keys.get(0);

            // check if heirarchy already exists or else create
            if (root.has(key) && root.get(key) instanceof JsonObject) {
                sub_keys.remove(key);
                //System.out.println("Going inside Key : "+key);
                addToJson(root.getAsJsonObject(key), sub_keys, val);
                //addToJson(root.getAsJsonObject(key), sub_keys, val);
            }
            else {
                root.add(key,new JsonObject());
                addToJson(root, sub_keys, val);
            }

        }
    }



}

