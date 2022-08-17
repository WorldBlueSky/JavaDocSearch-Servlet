import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.junit.Test;

import java.util.List;

public class MyTest {

    // 测试 ansj 中分词 Api的具体效果
    @Test
    public void testAnsj(){
        String str = "小明毕业于山东蓝翔技校，学习计算机专业，擅长使用挖掘机来炒方便面";
        List<Term> list =  ToAnalysis.parse(str).getTerms();
        for (Term term:list) {
            System.out.println(term.getName());
        }
    }
}
