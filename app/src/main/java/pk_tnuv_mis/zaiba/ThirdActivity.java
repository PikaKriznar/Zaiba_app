package pk_tnuv_mis.zaiba;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThirdActivity extends AppCompatActivity {

    private final Set<String> savedItems = new HashSet<>();
    private final List<CardItem> savedCardList = new ArrayList<>();
    private CardAdapter savedAdapter;
    private TextView nothingSavedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        DataManager.initialize(this);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        RecyclerView articlesRecyclerView = findViewById(R.id.articlesRecyclerView);
        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Article> articleList = loadArticlesFromJson();
        ArticleAdapter articleAdapter = new ArticleAdapter(articleList);
        articlesRecyclerView.setAdapter(articleAdapter);

        nothingSavedTextView = findViewById(R.id.nothing_saved);

        RecyclerView savedRecyclerView = findViewById(R.id.recyclerView);
        savedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        savedAdapter = new CardAdapter(savedCardList);
        savedRecyclerView.setAdapter(savedAdapter);

        updateNothingSavedTextVisibility();

        RecyclerView frogsRecyclerView = findViewById(R.id.recyclerView_2);
        frogsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<CardItem> frogsCardList = convertFrogsToCardItems(DataManager.getFrogs());
        CardAdapter frogsAdapter = new CardAdapter(frogsCardList);
        frogsRecyclerView.setAdapter(frogsAdapter);

        setupBottomMenu();
    }

    // Setup
    private void setupBottomMenu() {
        ImageButton homeButton = findViewById(R.id.menu_bar_button_home);
        ImageButton infoButton = findViewById(R.id.menu_bar_button_info);
        ImageButton settingsButton = findViewById(R.id.menu_bar_button_settings);

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ThirdActivity.this, MainActivity.class);
            startActivity(intent);
        });

        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(ThirdActivity.this, ThirdActivity.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ThirdActivity.this, FourthActivity.class);
            startActivity(intent);
        });
    }

    // Loaders
    private List<Article> loadArticlesFromJson() {
        List<Article> articleList = new ArrayList<>();
        try {
            InputStream is = getAssets().open("articles.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
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
                    }
                }
                String link = jsonObject.getString("link");

                articleList.add(new Article(title, text, boldProlog, article, imageResources, link));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articleList;
    }

    // Converters
    private List<CardItem> convertFrogsToCardItems(List<Frog> frogs) {
        List<CardItem> result = new ArrayList<>();
        for (Frog frog : frogs) {
            Integer imageResId = frog.getImageResource();
            result.add(new CardItem(
                    frog.getName(),
                    frog.getDescription(),
                    imageResId != null ? imageResId : R.drawable.info,
                    frog.getEstReadingTime(),
                    frog.getName()
            ));
        }
        return result;
    }

    // Classes
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
    static class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {
        private final List<Article> articleList;

        ArticleAdapter(List<Article> articleList) {
            this.articleList = articleList;
        }

        @NonNull
        @Override
        public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_card, parent, false);
            return new ArticleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
            Article item = articleList.get(position);
            if (item.getImageResources() != null && !item.getImageResources().isEmpty()) {
                holder.image.setImageResource(item.getImageResources().get(0));
            }
            holder.title.setText(item.getTitle());
            holder.source.setText(item.getLink());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FifthActivity.class);
                intent.putExtra("article_index", position);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return articleList.size();
        }

        static class ArticleViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title;
            TextView source;

            ArticleViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.article_image);
                title = itemView.findViewById(R.id.article_title);
                source = itemView.findViewById(R.id.article_source);
            }
        }
    }
    static class CardItem {
        String title;
        String description;
        Integer imageResId;
        String estReadingTime;
        String identifier;

        CardItem(String title, String description, Integer imageResId, String estReadingTime, String identifier) {
            this.title = title;
            this.description = description;
            this.imageResId = imageResId;
            this.estReadingTime = estReadingTime;
            this.identifier = identifier;
        }
    }
    class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
        private final List<CardItem> cardList;

        CardAdapter(List<CardItem> cardList) {
            this.cardList = cardList;
        }

        @NonNull
        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
            CardItem item = cardList.get(position);
            holder.title.setText(item.title);
            holder.description.setText(item.description);
            if (item.imageResId != null) {
                holder.image.setImageResource(item.imageResId);
            } else {
                holder.image.setImageResource(R.drawable.info);
            }
            holder.estTimeText.setText(item.estReadingTime != null ? "Est. time: " + item.estReadingTime : "Est. time: N/A");

            if (savedItems.contains(item.identifier)) {
                holder.saveButton.setImageResource(R.drawable.ic_bookmark_border);
                holder.saveButton.setColorFilter(getResources().getColor(R.color.text_heading, null)); // saved color
            } else {
                holder.saveButton.setImageResource(R.drawable.ic_bookmark_border);
                holder.saveButton.setColorFilter(getResources().getColor(R.color.light_grey_bar, null)); // unsaved color
            }

            // Add click listener to launch SixthActivity
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ThirdActivity.this, SixthActivity.class);
                intent.putExtra(SixthActivity.EXTRA_FROG_NAME, item.identifier);
                startActivity(intent);
            });

            holder.saveButton.setOnClickListener(v -> {
                if (savedItems.contains(item.identifier)) {
                    savedItems.remove(item.identifier);
                    holder.saveButton.setImageResource(R.drawable.ic_bookmark_border);
                    holder.saveButton.setColorFilter(getResources().getColor(R.color.light_grey_bar, null));
                    savedCardList.removeIf(card -> card.identifier.equals(item.identifier));
                } else {
                    savedItems.add(item.identifier);
                    holder.saveButton.setImageResource(R.drawable.ic_bookmark_border);
                    holder.saveButton.setColorFilter(getResources().getColor(R.color.text_heading, null));
                    if (!savedCardList.contains(item)) {
                        savedCardList.add(item);
                    }
                }
                // Update the visibility of the "nothing saved" message after modifying saved items
                updateNothingSavedTextVisibilityAfterChange();
            });
        }

        @Override
        public int getItemCount() {
            return cardList.size();
        }

        class CardViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title;
            TextView description;
            TextView estTimeText;
            ImageButton saveButton;

            CardViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.cardImage);
                title = itemView.findViewById(R.id.cardTitle);
                description = itemView.findViewById(R.id.cardDescription);
                estTimeText = itemView.findViewById(R.id.estTimeText);
                saveButton = itemView.findViewById(R.id.saveButton);
            }
        }
    }

    // Updaters
    private void updateNothingSavedTextVisibility() {
        if (savedCardList.isEmpty()) {
            nothingSavedTextView.setVisibility(View.VISIBLE);
        } else {
            nothingSavedTextView.setVisibility(View.GONE);
        }
    }
    private void updateNothingSavedTextVisibilityAfterChange() {
        updateNothingSavedTextVisibility();
        savedAdapter.notifyDataSetChanged();
    }
}