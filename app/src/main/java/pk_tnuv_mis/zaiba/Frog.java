package pk_tnuv_mis.zaiba;

import java.util.List;

public class Frog {
    private String name;
    private String latinName;
    private String image;
    private String description;
    private String basicDescription;
    private String habitat;
    private List<String> biologyStages;
    private String soundFile;
    private String estReadingTime;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getEstReadingTime() { return estReadingTime; }

    public Integer getImageResource() {
        return DataManager.getImageResource(image);
    }
}
