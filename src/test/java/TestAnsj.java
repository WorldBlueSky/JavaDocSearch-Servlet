import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.List;

public class TestAnsj {
    public static void main(String[] args) {
        String str = "小明毕业于山东蓝翔技校，学习计算机专业，擅长使用挖掘机来炒方便面";
        List<Term> list =  ToAnalysis.parse(str).getTerms();
        for (Term term:list) {
            System.out.println(term.getName());
        }
    }


}
