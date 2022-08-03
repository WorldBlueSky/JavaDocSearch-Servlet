import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 通过这个类在内存中构造出索引结构
public class Index {

    // 索引文件保存路径
    private static final String INDEX_PATH = "C:\\Users\\rain7\\Desktop\\";

    private ObjectMapper objectMapper = new ObjectMapper();

    // 使用数组下标表示 docId

    private ArrayList<DocInfo> forwardIndex = new ArrayList<>();

    // 使用哈希表表示倒排索引
    // key-->词
    // value --> 词关联的文章

    private HashMap<String,ArrayList<Weight>> invertedIndex = new HashMap<>();

    // 这个类要提供的方法
    //1、给定一个 docID,在正排索引当中查询文档的详细信息

    public DocInfo getDocInfo(int docId){

        return forwardIndex.get(docId); // o1复杂度，查询高效

    }

    //2、给定一个词，在倒排索引当中，查询那些文档与这个词相关联
    // 思考这里的返回值，单纯的返回整数的list是否可行呢？不太好
    // 词和文档之间存在一定的相关性，

    public List<Weight> getInverted(String term){

        return invertedIndex.get(term); // o1复杂度，查询高效

    }

    //3、往索引当中新增一个文档
    public void addDoc(String title,String url,String content){
        // 新增文档操作需要同时给正排索引和倒排索引新增信息

        // 构建正排索引
        DocInfo docInfo = buildForword(title,url,content);

        // 构建倒排索引
        buildInverted(docInfo);
    }

    private void buildInverted(DocInfo docInfo) {

        class WordCnt{
           //表示这个词在标题中出现的次数
           private int titleCount;
           //表示这个词在正文中出现的次数
           private int contentCount;
        }

        // 用来统计词频的数据结构
        HashMap<String,WordCnt> wordCntHashMap = new HashMap<>();

        //1、针对文档标题进行分词
        List<Term> terms = ToAnalysis.parse(docInfo.getTitle()).getTerms();

        //2、遍历分词结果，统计出每个词出现的次数
       for(Term term:terms){
           // 先判断term是否存在
           String word = term.getName();
           WordCnt wordCnt = wordCntHashMap.get(word);

          if(wordCnt==null){//如果不存在，就创建一个新的键值对，titleCount=1

              WordCnt newWordCnt = new WordCnt();
              newWordCnt.titleCount=1;
              newWordCnt.contentCount=0;
              wordCntHashMap.put(word,newWordCnt);

          }else{ //如果存在，就找到之前的值，对应的titleCount+1

              wordCnt.titleCount+=1;

          }
       }

        //3、针对正文进行分词
        terms = ToAnalysis.parse(docInfo.getContent()).getTerms();

        //4、遍历分词结果，统计每个词出现的次数
        for(Term term:terms){
            String word = term.getName();
            WordCnt wordCnt = wordCntHashMap.get(word);
            if(wordCnt==null){
                WordCnt newWordCnt = new WordCnt();
                newWordCnt.titleCount=0;
                newWordCnt.contentCount=1;
            }else {
                wordCnt.contentCount+=1;
            }
        }

        //5、把上面的结果汇总到一个 hashMap 当中

        // 最终文档的权重设定成 weight = 标题中出现的次数*10+ 正文中出现的次数（实际上公式很复杂，这个权重公式是拍脑门拍出来的）

        //6、遍历hashMap,依次更新倒排索引中的结构
        for(Map.Entry<String,WordCnt> entry:wordCntHashMap.entrySet()){

            synchronized (invertedIndex) {
                // 倒排拉链
                List<Weight> invertedList = invertedIndex.get(entry.getKey());

                if(invertedList==null){//如果为null，那就插入一个新的键值对

                    ArrayList<Weight> newInvertedList = new ArrayList<>();

                    // 把新的文档（当前 docInfo）构造成 weight对象，插入进来
                    Weight weight =  new Weight();
                    weight.setDocId(docInfo.getDocId());
                    // 权重计算公式=标题次数*10+正文次数
                    weight.setWeight(entry.getValue().titleCount*10+entry.getValue().contentCount);

                    newInvertedList.add(weight);

                    invertedIndex.put(entry.getKey(),newInvertedList);

                }else{//如果非空那么就把当前文档，构造出一个weight对象，插入到倒排拉链的后面
                    Weight weight =  new Weight();
                    weight.setDocId(docInfo.getDocId());
                    // 权重计算公式=标题次数*10+正文次数
                    weight.setWeight(entry.getValue().titleCount*10+entry.getValue().contentCount);
                    invertedList.add(weight);
                }
            }
        }

    }

    private DocInfo buildForword(String title,String url,String content) {

        // 将解析的文档内容构造成一个类
        DocInfo docInfo = new DocInfo();
        docInfo.setTitle(title);
        docInfo.setUrl(url);
        docInfo.setContent(content);

        synchronized (forwardIndex) {
            // 新加入的docId 放在 forwordIndex 数组的最后，所以id就是数组的长度
            docInfo.setDocId(forwardIndex.size());

            // 往正排索引中插入文档数据
            forwardIndex.add(docInfo);
        }
        return docInfo;
    }

    //4、把内存当中的索引结构保存到磁盘当中
    public void save(){

        System.out.println("保存索引开始!");
        long start = System.currentTimeMillis();


        // 1、先判断索引文件保存的路径是否存在
        File file = new File(INDEX_PATH);
        if(!file.exists()){
            file.mkdirs();
        }

        File forwordIndexFile = new File(INDEX_PATH+"forword.txt");
        File invertedIndexFile = new File(INDEX_PATH+"inverted.txt");


        try {

            objectMapper.writeValue(forwordIndexFile,forwardIndex);
            objectMapper.writeValue(invertedIndexFile,invertedIndex);

        } catch (IOException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        System.out.println("保存索引完成!");

        System.out.println("保存消耗时间为 "+(end-start)+" ms");
    }

    //5、把磁盘中的索引数据加载到内存当中
    public void load(){
        System.out.println("加载索引开始!");
        long start = System.currentTimeMillis();

        //1、设置加载索引的路径
        File forwordFile = new File(INDEX_PATH+"forword.txt");
        File invertedFile = new File(INDEX_PATH+"inverted.txt");

        //2、从文件中解析索引数据
        try {

            forwardIndex = objectMapper.readValue(forwordFile,new TypeReference<ArrayList<DocInfo>>(){});
            invertedIndex = objectMapper.readValue(invertedFile,new TypeReference<HashMap<String,ArrayList<Weight>>>(){});

        } catch (IOException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        System.out.println("加载索引结束!");

        System.out.println("加载消耗时间为 "+(end-start)+" ms");
    }

}
