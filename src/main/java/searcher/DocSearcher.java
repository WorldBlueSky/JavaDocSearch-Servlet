package searcher;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import static org.ansj.splitWord.analysis.ToAnalysis.parse;

// 通过这个类，来完成整个的搜索流程
public class DocSearcher {
    // 此处要加上索引对象的实例
    private Index index = new Index();

    public DocSearcher() {
        //在构造方法的时候进行加载索引
        index.load();
    }

    // 完成整个搜索过程的方法
    // 参数（输入部分） 用户给出的查询词
    // 返回值（输出部分）返回的包装类型的搜索结果
    public List<Result> search(String query){

        //1、【分词】针对 query 查询词进行分词
        List<Term> terms = ToAnalysis.parse(query).getTerms();

        //2、【触发】针对分词结果进行查倒排
        List<Weight> allTermResult = new ArrayList<>();
        for (Term term:terms) {
            String word = term.getName();
            List<Weight> invertedList = index.getInverted(word);//根据查询词进行查倒排
            //虽然倒排索引中有很多的词，但是这里的词一定是之前解析的文档中已经存在的
            // 但是如果word在倒排索引中查找不到的话
            if(invertedList==null){
                 continue;//跳过
            }
            allTermResult.addAll(invertedList);//批量追加一组元素，所以是addAll()
        }

        //3、【排序】针对触发的结果按照相关程度进行降序排序
        allTermResult.sort(new Comparator<Weight>() {
            @Override
            public int compare(Weight o1, Weight o2) {
                // 重写比较器，降序排序
                return o2.getWeight()-o1.getWeight();
            }
        });

        //4、【包装结果】针对排序的结果，去查正排，构造出要返回的数据.
        List<Result> results = new ArrayList<>();
        for(Weight weight:allTermResult){
            DocInfo docInfo = index.getDocInfo(weight.getDocId());
            Result result = new Result();
            result.setTitle(docInfo.getTitle());
            result.setUrl(docInfo.getUrl());
            //描述是 正文的一段内容的摘要，得包含查询词或者查询词的一部分
            result.setDesc(GenDesc(docInfo.getContent(),terms));
            //可以获取到所有的查询词结果
            // 遍历分词结果，看那个结果在正文中出现

            // 就针对这个包含的分词结果，去正文中查找，找到对应的位置，以这个词的位置为中心
            // 往前截取60个字符，然后再以描述开始往后截取160个字符作为整个秒描述
            results.add(result);
        }

         return results;
    }

    private String GenDesc(String content, List<Term> terms) {
        // 遍历分词结果，看看哪个分词结果在content结果中先出现
        // 只体现描述与分词具有相关性即可，找到第一个出现的分词即可break
        int firstPos = -1;
        for(Term term:terms){
            // 在分词库直接针对词进行转小写了，但是正文中不一定都是小写的字符
            // 必须把正文先转成小写的在进行查询位置
            String word = term.getName();
            // 此处需要 " 全字匹配 "，word独立成词才能查找出来，而不是作为词的一部分 ArrayList List 老婆 老婆饼
            firstPos = content.toLowerCase().indexOf(" "+word+" ");// 在正文中找到这个词的位置
            if(firstPos!=-1){
                // 说明这个词找到了,直接break
                break;
            }
        }

        if(firstPos==-1){
            // 所有的分词结果都不在正文中存在
            // 这是属于比较极端的情况
            // 返回一个正文的前160个字符即可
            return content.substring(0,160)+"...";
        }

        // 从firstPos作为基准位置，往前找60个字符作为起始位置
        String desc="";

        int descBeg = firstPos<100?0:firstPos-100;
        if(descBeg+200>content.length()){
            desc = content.substring(descBeg);
        }else{
            desc = content.substring(descBeg,descBeg+200)+"...";
        }

        return desc;
    }

    public static void main(String[] args) {
        DocSearcher docSearcher = new DocSearcher();
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print("->");
            String query = scanner.next();
            List<Result> results = docSearcher.search(query);
            for (Result result:results) {
                System.out.println(result.toString());
                System.out.println("================================");
            }
        }
    }
}
