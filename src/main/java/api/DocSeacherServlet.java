package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import search.DocSearcher;
import search.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/searcher")
public class DocSeacherServlet extends HttpServlet {
    //此处的对象也是全局唯一的，static修饰
    private static DocSearcher docSearcher = new DocSearcher();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1、先解析请求，拿到用户提交的查询词
        String query = req.getParameter("query");
        //2、判断是否为空
        if(query==null ||query.equals("")){
           String msg = "参数非法，未获取到query!";
            resp.sendError(404,msg);
            return;
        }
        System.out.println(query);
        //3、调用搜索模块进行搜索，返回搜索结果
        List<Result> results = docSearcher.search(query);

        //4、将搜索结果进行打包
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        objectMapper.writeValue(resp.getWriter(),results);
    }
}
