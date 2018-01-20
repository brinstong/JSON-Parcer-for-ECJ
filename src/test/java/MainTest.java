import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class MainTest {

    @Test
    public void paramsToJson() {



        File input1= new File("sum.params");
        File input2= new File("bbob.params");
        File input3= new File("tutorial3.params");
        File input4= new File("highdimension.params");
        File input5= new File("klandscapes.params");


        File output1= new File("sum.json");
        File output2= new File("bbob.json");
        File output3= new File("tutorial3.json");
        File output4= new File("highdimension.json");
        File output5= new File("klandscapes.json");


        try {
            Main.paramsToJson(input1,output1);
            Main.paramsToJson(input2,output2);
            Main.paramsToJson(input3,output3);
            Main.paramsToJson(input4,output4);
            Main.paramsToJson(input5,output5);

            System.out.println("Now checking equality of "+input1.getName()+" with "+output1.getName());
            assertTrue(Main.equalityCheck(output1,input1));
            System.out.println("Now checking equality of "+input2.getName()+" with "+output2.getName());
            assertTrue(Main.equalityCheck(output2,input2));
            System.out.println("Now checking equality of "+input3.getName()+" with "+output3.getName());
            assertTrue(Main.equalityCheck(output3,input3));
            System.out.println("Now checking equality of "+input4.getName()+" with "+output4.getName());
            assertTrue(Main.equalityCheck(output4,input4));
            System.out.println("Now checking equality of "+input5.getName()+" with "+output5.getName());
            assertTrue(Main.equalityCheck(output5,input5));


        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during Reading or Writing File");
        }

    }


    @Test
    public void jsonToParams() {


        File input1= new File("sum.json");
        File input2= new File("bbob.json");
        File input3= new File("tutorial3.json");
        File input4= new File("highdimension.json");
        File input5= new File("klandscapes.json");

        File output1= new File("sum.params");
        File output2= new File("bbob.params");
        File output3= new File("tutorial3.params");
        File output4= new File("highdimension.params");
        File output5= new File("klandscapes.params");




        try {

            Main.JsonToParams(input1,output1);
            Main.JsonToParams(input2,output2);
            Main.JsonToParams(input3,output3);
            Main.JsonToParams(input4,output4);
            Main.JsonToParams(input5,output5);


            System.out.println("Now checking equality of "+input1.getName()+" with "+output1.getName());
            assertTrue(Main.equalityCheck(input1,output1));
            System.out.println("Now checking equality of "+input2.getName()+" with "+output2.getName());
            assertTrue(Main.equalityCheck(input2,output2));
            System.out.println("Now checking equality of "+input3.getName()+" with "+output3.getName());
            assertTrue(Main.equalityCheck(input3,output3));
            System.out.println("Now checking equality of "+input4.getName()+" with "+output4.getName());
            assertTrue(Main.equalityCheck(input4,output4));
            System.out.println("Now checking equality of "+input5.getName()+" with "+output5.getName());
            assertTrue(Main.equalityCheck(input5,output5));


        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during Reading or Writing File");
        }

    }

}