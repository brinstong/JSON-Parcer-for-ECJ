import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

public class Temp {

    public static void main(String arg[]) throws IOException {

        File inputFile = new File("highdimension.params");
        File outputFile = new File("highdimension.json");
        String json = paramsToJson(inputFile, outputFile).toString();

        System.out.println(json);
/*

        JsonObject a = new JsonObject();
        a.addProperty("","a");
        JsonObject b = new JsonObject();
        b.addProperty("","b");
        JsonObject c = new JsonObject();
        c.addProperty("","c");
        JsonObject d = new JsonObject();
        d.addProperty("","d");
        JsonObject e = new JsonObject();
        e.addProperty("","e");


        JsonArray jsonArray = new JsonArray();

        System.out.println(jsonArray);
        jsonArray.add(new JsonObject());
        jsonArray.set(0,a);
        System.out.println(jsonArray);
        jsonArray.add(new JsonObject());
        jsonArray.set(1,b);
        System.out.println(jsonArray);
        jsonArray.add(new JsonObject());
        jsonArray.set(2,c);
        System.out.println(jsonArray);
        jsonArray.set(1,d);
        System.out.println(jsonArray);

*/


    }

    public static JsonObject paramsToJson(File paramsFile, File outputFile) throws IOException {
        HashMap<String, String> keyVals = new HashMap<>();

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(paramsFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            keyVals.put(key, value);
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


    private static int getNum(String num) {

        // returns -1 if not numeric


        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException nfe) {
            return -1;
        }


    }

    private static void addToJson(JsonElement json_root, LinkedList<String> sub_keys, String val) {

        System.out.println("Json Root : " + json_root);
        System.out.println("Keys : " + sub_keys);
        System.out.println("Val : " + val);


        if (json_root.isJsonObject()) {

            JsonObject root = json_root.getAsJsonObject();
            String key = sub_keys.get(0);

            if (getNum(key) == -1) {
                //Non numeric key
                if (sub_keys.size() == 1) {
                    root.addProperty(key, val);


                } else {

                    if (!root.has(key)) {


                        String next_key = sub_keys.get(1);

                        if (getNum(next_key) == -1) {

                            if (!root.has(key)) {

                                root.add(key, new JsonObject());
                            }

                        } else {
                            root.add(key, new JsonArray());


                        }
                    }

                    sub_keys.remove(key);


                    addToJson(root.get(key), sub_keys, val);


                }
            } else {

                System.err.println("Found numeric heirarchy without array");


            }

        } else if (json_root.isJsonArray()) {


            JsonArray root = json_root.getAsJsonArray();
            String key = sub_keys.get(0);

            if (getNum(key) == -1) {
                System.err.println("Found non numeric key with Array");
            } else {
                // Numeric key
                // if its a numeric key, the current root must be a JsonArray

                if (sub_keys.size() == 1) {

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("", val);

                    try {
                        root.set(getNum(key), jsonObject);
                    }catch (IndexOutOfBoundsException ex) {
                        int size = getNum(key);
                        while (root.size()<=size) {
                            //System.out.println(root);
                            //System.out.println(key +"\t"+val);
                            root.add(new JsonObject());
                        }


                        root.set(size, jsonObject);
                    }
                } else {


                    String next_key = sub_keys.get(1);

                    int size = getNum(key);

                    if (getNum(next_key) == -1) {



                        try {
                            root.set(size, new JsonObject());

                        }catch (IndexOutOfBoundsException ex) {




                            while (root.size() <= size) {
                                root.add(new JsonObject());
                            }
                            //root.add(new JsonObject());
                            root.set(size, new JsonObject());
                        }

                    } else {

                        try {
                            root.set(size, new JsonObject());

                        }catch (IndexOutOfBoundsException ex) {


                            while (root.size() <= size) {
                                root.add(new JsonArray());
                            }

                            //root.add(new JsonArray());
                            root.set(getNum(key), new JsonArray());
                        }

                    }


                    sub_keys.remove(key);

                    addToJson(root.get(getNum(key)), sub_keys, val);



                }


            }


        } else {
            System.err.println("Problem while Parsing : " + sub_keys + "\t" + val);
        }


    }


}


