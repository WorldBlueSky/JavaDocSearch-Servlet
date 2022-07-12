import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.List;

public class MyTest {
    public static void main(String[] args) {
        String str = "小明吃了一个鸡肉卷然后放了一个屁";
        List<Term> list =  ToAnalysis.parse(str).getTerms();
        for (Term term:list) {
            System.out.println(term.getName());
        }
    }


}
