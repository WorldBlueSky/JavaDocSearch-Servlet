package searcher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocInfo {
    private int docId;
    private String title;
    private String url;
    private String content;
}
