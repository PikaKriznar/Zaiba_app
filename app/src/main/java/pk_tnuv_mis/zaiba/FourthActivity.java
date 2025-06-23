package pk_tnuv_mis.zaiba;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

public class FourthActivity extends AppCompatActivity {

    private boolean isSoundOn = true;
    private int volume = 50;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(FourthActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        SeekBar volumeSeekBar = findViewById(R.id.volume_seek_bar);
        TextView volumeValue = findViewById(R.id.volumeValue);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(volume * maxVolume / 100);
        volumeValue.setText("Volume: " + volume + "%");

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = progress * 100 / maxVolume;
                volumeValue.setText("Volume: " + volume + "%");
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SwitchCompat soundSwitch = findViewById(R.id.soundSwitch);
        soundSwitch.setChecked(isSoundOn);
        setupSwitchColors(soundSwitch);
    }

    // Helper methods
    private void setupSwitchColors(SwitchCompat soundSwitch) {
        int g1Color = ContextCompat.getColor(this, R.color.g1);
        int grayColor = ContextCompat.getColor(this, android.R.color.darker_gray);
        updateSwitchColors(soundSwitch, isSoundOn, g1Color, grayColor);

        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSoundOn = isChecked;
            soundSwitch.setText(isChecked ? "Sound On" : "Sound Off");
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, !isChecked);
            updateSwitchColors(soundSwitch, isChecked, g1Color, grayColor);
        });
    }
    private void updateSwitchColors(SwitchCompat soundSwitch, boolean isChecked, int onColor, int offColor) {
        if (isChecked) {
            soundSwitch.setThumbTintList(ColorStateList.valueOf(onColor));
            soundSwitch.setTrackTintList(ColorStateList.valueOf(onColor));
        } else {
            soundSwitch.setThumbTintList(ColorStateList.valueOf(offColor));
            soundSwitch.setTrackTintList(ColorStateList.valueOf(offColor));
        }
    }
}