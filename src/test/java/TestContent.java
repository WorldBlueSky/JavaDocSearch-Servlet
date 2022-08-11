import java.io.*;

public class TestContent {

    public static void main(String[] args) {
        Parser parser = new Parser();
        File file = new File("C:\\Users\\rain7\\Desktop/docs/api/java/awt/List.html");
        System.out.println(parser.parseContent(file).substring(0,200));
        System.out.println("===========================");
        System.out.println(parser.parseContentByRegex(file).substring(0,200));
    }

}
