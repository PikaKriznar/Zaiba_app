package pk_tnuv_mis.zaiba;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Article {
    private String title;
    private String text;
    @SerializedName("images")
    private List<String> imageNames;
    private String link;

    // Getters
    public String getText() { return text; }

}
