package pk_tnuv_mis.zaiba;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FifthActivity extends AppCompatActivity {

    private TextView titleTextView, prologTextView, articleTextView, linkTextView;
    private TextView smallTitleTextView;
    private ImageView articleImageView;
    private List<Article> articles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fifth);

        titleTextView = findViewById(R.id.article_title);
        prologTextView = findViewById(R.id.article_prolog);
        articleTextView = findViewById(R.id.article_text);
        linkTextView = findViewById(R.id.article_link);
        articleImageView = findViewById(R.id.article_image);
        smallTitleTextView = findViewById(R.id.location_smallTitle);


        articles = loadArticlesFromJson();
        if (articles == null) {
            titleTextView.setText("Error loading articles");
            return;
        }

        Intent intent = getIntent();
        int articleIndex = intent.getIntExtra("article_index", 0);
        if (articleIndex >= 0 && articleIndex < articles.size()) {
            displayArticle(articles.get(articleIndex));
        } else {
            titleTextView.setText("Article not found");
        }

        setupBottomMenu();
        setupBackButton();

    }

    // Helper methods
    private List<Article> loadArticlesFromJson() {
        List<Article> articleList = new ArrayList<>();
        try {
            InputStream is = getAssets().open("articles.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String title = jsonObject.getString("title");
                String text = jsonObject.getString("text");
                JSONObject actualText = jsonObject.getJSONObject("actualText");
                String boldProlog = actualText.getString("boldProlog");
                String article = actualText.getString("article");
                JSONArray images = jsonObject.getJSONArray("images");
                List<Integer> imageResources = new ArrayList<>();
                for (int j = 0; j < images.length(); j++) {
                    String imageName = images.getString(j);
                    int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    if (resId != 0) {
                        imageResources.add(resId);
                    } else {
                        imageResources.add(R.drawable.info);
                    }
                }
                String link = jsonObject.getString("link");

                articleList.add(new Article(title, text, boldProlog, article, imageResources, link));
            }
        } catch (IOException | JSONException e) {
            Log.e("FifthActivity", "Error loading JSON", e);
            return null;
        }
        return articleList;
    }
    @SuppressLint("SetTextI18n")
    private void displayArticle(Article article) {
        titleTextView.setText(article.getTitle());
        prologTextView.setText(article.getBoldProlog());
        articleTextView.setText(article.getArticle());
        linkTextView.setText("Link: " + article.getLink());
        if (article.getImageResources() != null && !article.getImageResources().isEmpty()) {
            articleImageView.setImageResource(article.getImageResources().get(0));
        } else {
            articleImageView.setImageResource(R.drawable.info);
        }
        if (smallTitleTextView != null) {
            smallTitleTextView.setText(article.getTitle());
        }
    }

    // Setup
    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }
    private void setupBottomMenu() {
        ImageButton homeButton = findViewById(R.id.menu_bar_button_home);
        ImageButton infoButton = findViewById(R.id.menu_bar_button_info);
        ImageButton settingsButton = findViewById(R.id.menu_bar_button_settings);

        homeButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(FifthActivity.this, MainActivity.class);
            startActivity(intent1);
        });

        infoButton.setOnClickListener(v -> {
            Intent intent2 = new Intent(FifthActivity.this, ThirdActivity.class);
            startActivity(intent2);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent3 = new Intent(FifthActivity.this, FourthActivity.class);
            startActivity(intent3);
        });
    }

    static class Article {
        private final String title;
        private final String text;
        private final String boldProlog;
        private final String article;
        private final List<Integer> imageResources;
        private final String link;

        Article(String title, String text, String boldProlog, String article, List<Integer> imageResources, String link) {
            this.title = title;
            this.text = text;
            this.boldProlog = boldProlog;
            this.article = article;
            this.imageResources = imageResources;
            this.link = link;
        }

        public String getTitle() { return title; }
        public String getText() { return text; }
        public String getBoldProlog() { return boldProlog; }
        public String getArticle() { return article; }
        public List<Integer> getImageResources() { return imageResources; }
        public String getLink() { return link; }
    }
}