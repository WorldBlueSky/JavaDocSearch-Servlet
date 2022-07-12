import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TestContent {
    public static void main(String[] args) {
        File file = new File("C:\\Users\\rain7\\Desktop\\docs\\api\\java\\lang\\class-use\\Appendable.html");
        String content = parseContent(file);
        System.out.println(content);
    }

    private static String parseContent(File file) {
        try {

            FileReader fileReader = new FileReader(file);
            // 是否要拷贝的开关
            boolean isCopy = true;

            StringBuilder content = new StringBuilder();

            while(true){
                int ret =  fileReader.read();
                if(ret==-1){
                    break;
                }
                //如果不是-1，那么就是一个合法的字符
                char c = (char)ret;

                // 对字符进行识别，判断拷贝开关是否开启关闭
                if(isCopy){ // 如果拷贝开关为true，进入条件中

                    if(c=='<'){//如果碰到<,那么关闭开关
                        isCopy=false;
                        continue;
                    }

                    if(c=='\r' || c=='\n'){// 经过测试查看，发现原文中有很多换行符，所以去除，方便后续截取摘要
                        c=' ';
                    }

                    // 如果不是左括号，同时开关是开的，那么拷贝字符
                     content.append(c);

                }else{ //如果拷贝开关为false,那么跳过不拷贝

                    //如果字符为>,那么拷贝开关打开
                    if(c=='>'){
                        isCopy=true;
                    }
                }
            }

            return content.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
