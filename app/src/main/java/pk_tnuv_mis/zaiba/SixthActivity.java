package pk_tnuv_mis.zaiba;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import android.media.MediaPlayer;
import java.util.Random;

public class SixthActivity extends AppCompatActivity {

    private TextView frogTitle, scientificName, frogDescription, frogHabitat, topTitle;
    private ImageView frogImage;
    MediaPlayer mediaPlayer;

    ImageButton playButton, stopButton;
    int[] soundFiles = { R.raw.bombina_variegata,
            R.raw.bufo_bufo,
            R.raw.bufo_viridis,
            R.raw.hyla_arboea,
            R.raw.rana_lastastai,
            R.raw.rana_ridibunda,
            R.raw.rana_temporaria, };



    public static final String EXTRA_FROG_NAME = "frog_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sixth);

        setupBottomMenu();

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            stopAudio();
            finish();
        });


        topTitle =findViewById(R.id.location_smallTitle);
        frogTitle = findViewById(R.id.frogTitle);
        scientificName = findViewById(R.id.scientificName);
        frogImage = findViewById(R.id.frogImage);
        frogDescription = findViewById(R.id.frogDescription);
        frogHabitat = findViewById(R.id.frogHabitat);
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);

        String frogName = getIntent().getStringExtra(EXTRA_FROG_NAME);
        if (frogName == null) {
            Toast.makeText(this, "Error: Frog name not provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupArticle(frogName);
        playAudio();
    }




    // Helper methods
    private String loadJSONFromAsset(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e("SixthActivity", "Error reading JSON file", e);
            return null;
        }
    }
    private void stopAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        playButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.GONE);
    }

    // Setup
    private void setupBottomMenu() {
        ImageButton homeButton = findViewById(R.id.menu_bar_button_home);
        ImageButton infoButton = findViewById(R.id.menu_bar_button_info);
        ImageButton settingsButton = findViewById(R.id.menu_bar_button_settings);

        homeButton.setOnClickListener(v -> {
            stopAudio();
            Intent homeIntent = new Intent(SixthActivity.this, MainActivity.class);
            startActivity(homeIntent);
            stopAudio();
        });

        infoButton.setOnClickListener(v -> {
            stopAudio();
            Intent infoIntent = new Intent(SixthActivity.this, ThirdActivity.class);
            startActivity(infoIntent);
        });

        settingsButton.setOnClickListener(v -> {
            stopAudio();
            Intent settingsIntent = new Intent(SixthActivity.this, FourthActivity.class);
            startActivity(settingsIntent);

        });
    }
    private void setupArticle(String frogName) {
        try {
            String jsonString = loadJSONFromAsset("frogs.json");
            JSONArray frogsArray = new JSONArray(jsonString);

            JSONObject selectedFrog = null;
            for (int i = 0; i < frogsArray.length(); i++) {
                JSONObject frog = frogsArray.getJSONObject(i);
                if (frog.getString("name").equals(frogName)) {
                    selectedFrog = frog;
                    break;
                }
            }

            if (selectedFrog != null) {
                topTitle.setText(selectedFrog.getString("name"));
                frogTitle.setText(selectedFrog.getString("name"));
                scientificName.setText(selectedFrog.getString("latinName"));
                frogDescription.setText(selectedFrog.getString("basicDescription"));
                frogHabitat.setText(selectedFrog.getString("habitat"));

                String imageName = selectedFrog.getString("image");
                int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                if (imageResId != 0) {
                    frogImage.setImageResource(imageResId);
                } else {
                    Log.w("SixthActivity", "Image resource not found: " + imageName);
                }

            } else {
                Log.e("SixthActivity", frogName + " not found in JSON");
                Toast.makeText(this, "Error: Frog data not found", Toast.LENGTH_LONG).show();
                finish();
            }

        } catch (Exception e) {
            Log.e("SixthActivity", "Error parsing JSON", e);
            Toast.makeText(this, "Error loading frog data", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    private void playAudio() {
        playButton.setOnClickListener(v -> {
            stopAudio();

            int randomIndex = new Random().nextInt(soundFiles.length);
            int selectedSound = soundFiles[randomIndex];

            mediaPlayer = MediaPlayer.create(this, selectedSound);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                playButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);

                mediaPlayer.setOnCompletionListener(mp -> stopAudio());
            } else {
                Toast.makeText(this, "Playback error", Toast.LENGTH_SHORT).show();
            }
        });

        stopButton.setOnClickListener(v -> stopAudio());
    }
}