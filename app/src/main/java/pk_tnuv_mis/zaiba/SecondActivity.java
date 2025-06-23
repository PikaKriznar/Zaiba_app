package pk_tnuv_mis.zaiba;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SecondActivity extends AppCompatActivity {
    RelativeLayout soundOverlay;
    TextView frogName, timer, duration;
    ImageButton btnPlay, btnClose;
    VideoView videoWaveform;
    Handler handler = new Handler();
    enum TimeFilter { TODAY, WEEKLY, MONTHLY }
    TimeFilter currentFilter = TimeFilter.TODAY;
    private Button selectedSpeciesButton = null;
    private CalendarAndGraphHelper helper;
    private static final String CURRENT_MONTH = "May 2025";
    private ConstraintLayout currentOverlay = null;
    private static final String[] SPECIES_NAMES = {
            "Navadna Krastača", "Zelena Rega", "Sekulja"
    };
    private static final int[] DOT_COLORS = {
            R.color.g3, R.color.g4, R.color.g5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        helper = new CalendarAndGraphHelper(this);

        TextView totalTop = findViewById(R.id.variableText);
        setTotalFrogCallsDetectedNumber(totalTop);
        setupBottomMenu();
        video();

        currentFilter = TimeFilter.TODAY;
        updateViewForFilter();

        ImageButton backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        updateDonutData();

        soundOverlay = findViewById(R.id.sound_overlay);
        frogName = findViewById(R.id.frog_name);
        timer = findViewById(R.id.timer);
        duration = findViewById(R.id.duration);
        btnPlay = findViewById(R.id.btn_play);
        btnClose = findViewById(R.id.btn_close);
        videoWaveform = findViewById(R.id.video_waveform);

        View loudestFrogCard = findViewById(R.id.loudest_frog_card);
        if (loudestFrogCard != null) {
            loudestFrogCard.setOnClickListener(v -> showSoundOverlayAndPlayVideo());
        }

        Intent intent = getIntent();
        playNClose();
        titles(intent);

        setupTimeButtons();
        setupSpeciesButtons();
    }

    // Setup
    private void setupBottomMenu() {
        ImageButton homeButton = findViewById(R.id.menu_bar_button_home);
        ImageButton infoButton = findViewById(R.id.menu_bar_button_info);
        ImageButton settingsButton = findViewById(R.id.menu_bar_button_settings);

        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent homeIntent = new Intent(SecondActivity.this, MainActivity.class);
                startActivity(homeIntent);
            });
        }

        if (infoButton != null) {
            infoButton.setOnClickListener(v -> {
                Intent infoIntent = new Intent(SecondActivity.this, ThirdActivity.class);
                startActivity(infoIntent);
            });
        }

        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                Intent settingsIntent = new Intent(SecondActivity.this, FourthActivity.class);
                startActivity(settingsIntent);
            });
        }
    }
    private void playNClose() {
        if (btnPlay != null) {
            btnPlay.setOnClickListener(v -> {
                if (videoWaveform != null) {
                    if (videoWaveform.isPlaying()) {
                        videoWaveform.pause();
                        btnPlay.setImageResource(android.R.drawable.ic_media_play);
                    } else {
                        videoWaveform.start();
                        btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                        updateTimer();
                    }
                    soundOverlay.setVisibility(View.VISIBLE);
                }
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                if (videoWaveform != null && videoWaveform.isPlaying()) {
                    videoWaveform.stopPlayback();
                }
                if (soundOverlay != null) {
                    soundOverlay.setVisibility(View.GONE);
                }
            });
        }
    }
    private void titles(Intent intent) {
        String frog = intent.getStringExtra("frog_name");
        if (frogName != null && frog != null) {
            frogName.setText(frog);
        }

        TextView cityNameTextView = findViewById(R.id.location_smallTitle);
        TextView locationNameTextView = findViewById(R.id.location_bigTitle);

        String cityName = intent.getStringExtra("CITY_NAME");
        String locationName = intent.getStringExtra("LOCATION_NAME");

        if (cityNameTextView != null && cityName != null) {
            cityNameTextView.setText(cityName);
        }
        if (locationNameTextView != null && locationName != null) {
            locationNameTextView.setText(locationName);
        }
    }
    private void setupTimeButtons() {
        Button btnToday = findViewById(R.id.location_today_btn);
        Button btnWeekly = findViewById(R.id.location_weekly_btn);
        Button btnMonthly = findViewById(R.id.location_monthly_btn);

        if (btnToday != null) {
            btnToday.setOnClickListener(v -> handleTimeFilterButtonClick(btnToday, TimeFilter.TODAY));
        }
        if (btnWeekly != null) {
            btnWeekly.setOnClickListener(v -> handleTimeFilterButtonClick(btnWeekly, TimeFilter.WEEKLY));
        }
        if (btnMonthly != null) {
            btnMonthly.setOnClickListener(v -> handleTimeFilterButtonClick(btnMonthly, TimeFilter.MONTHLY));
        }
    }
    private void setupSpeciesButtons() {
        Button btnSpeciesA = findViewById(R.id.location_nav_krastaca_btn);
        Button btnSpeciesB = findViewById(R.id.location_zel_rega_btn);
        Button btnSpeciesC = findViewById(R.id.location_sekulja_btn);
        Button[] speciesButtons = { btnSpeciesA, btnSpeciesB, btnSpeciesC };

        if (btnSpeciesA != null) {
            btnSpeciesA.setOnClickListener(v -> {
                handleSpeciesButtonClick(btnSpeciesA, speciesButtons);
                if (currentFilter == TimeFilter.MONTHLY) {
                    updateViewForFilter();
                } else {
                    updateBarGraphs();
                }
            });
        }

        if (btnSpeciesB != null) {
            btnSpeciesB.setOnClickListener(v -> {
                handleSpeciesButtonClick(btnSpeciesB, speciesButtons);
                if (currentFilter == TimeFilter.MONTHLY) {
                    updateViewForFilter();
                } else {
                    updateBarGraphs();
                }
            });
        }

        if (btnSpeciesC != null) {
            btnSpeciesC.setOnClickListener(v -> {
                handleSpeciesButtonClick(btnSpeciesC, speciesButtons);
                if (currentFilter == TimeFilter.MONTHLY) {
                    updateViewForFilter();
                } else {
                    updateBarGraphs();
                }
            });
        }
    }
    private void setTotalFrogCallsDetectedNumber(TextView textView) {
        Random random = new Random();
        int randomNumber = 300 + random.nextInt(1000);

        String label = "Frog calls detected: ";
        String number = String.valueOf(randomNumber);

        SpannableString spannable = new SpannableString(label + number);
        spannable.setSpan(
                new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                label.length(),
                label.length() + number.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        textView.setText(spannable);
    }


    // Calendar Day Overlay
    @SuppressLint("SetTextI18n")
    private void showSpeciesOverlay(int day) {
        if (currentOverlay != null || isAfterToday(day)) {
            return;
        }

        View overlayView = LayoutInflater.from(this).inflate(R.layout.calendar_view_overlay, null);
        ConstraintLayout overlayContainer = overlayView.findViewById(R.id.overlay_container);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        overlayContainer.setLayoutParams(layoutParams);

        TextView speciesCountText = overlayView.findViewById(R.id.speciesCountText);
        String speciesData = getSpeciesDataForDay(day);
        speciesCountText.setText(speciesData);

        Button closeButton = overlayView.findViewById(R.id.closeOverlayButton);
        closeButton.setOnClickListener(v -> removeOverlay(overlayContainer));
        Button shareButton = overlayView.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Frog Species Data for Day " + day);
            shareIntent.putExtra(Intent.EXTRA_TEXT, speciesData);
            startActivity(Intent.createChooser(shareIntent, "Share Species Data"));
            Log.d("ShareButton", "Share intent started with data: " + speciesData);
        });

        ViewGroup rootLayout = findViewById(R.id.activity_second_root_layout);
        if (rootLayout != null) {
            rootLayout.addView(overlayContainer);
            currentOverlay = overlayContainer;
        }
    }
    private String getSpeciesDataForDay(int day) {
        try {
            String jsonFile = helper.getJsonFileForMonth(CURRENT_MONTH);
            InputStream is = getAssets().open(jsonFile);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONObject json = new JSONObject(new String(buffer, "UTF-8"));
            JSONObject dayData = json.getJSONObject(CURRENT_MONTH).getJSONObject(String.valueOf(day));
            return "Day " + day + " species count:\n" +
                    "Navadna krastača: " + dayData.getInt("navadna krastača") + "\n" +
                    "Zelena rega: " + dayData.getInt("zelena rega") + "\n" +
                    "Sekulja: " + dayData.getInt("sekulja");
        } catch (Exception e) {
            Log.e("SecondActivity", "Error reading JSON for day " + day, e);
            return "Day " + day + " species count:\nError loading data";
        }
    }
    private void removeOverlay(ViewGroup overlay) {
        if (overlay.getParent() != null) {
            ((ViewGroup) overlay.getParent()).removeView(overlay);
            currentOverlay = null;
        }
    }

    // Filter, graphs and calendar logic
    private void updateViewForFilter() {
        Log.d("SecondActivity", "updateViewForFilter: Current filter is " + currentFilter);
        Button btnToday = findViewById(R.id.location_today_btn);
        Button btnWeekly = findViewById(R.id.location_weekly_btn);
        Button btnMonthly = findViewById(R.id.location_monthly_btn);

        highlightSelectedFilterButton(btnToday, btnWeekly, btnMonthly);

        View barGraphContainer = findViewById(R.id.graphContainer);
        View calendarGrid = findViewById(R.id.calendarView);

        if (currentFilter == TimeFilter.MONTHLY) {
            if (barGraphContainer != null) {
                barGraphContainer.setVisibility(View.GONE);
            }
            if (calendarGrid != null) {
                calendarGrid.setVisibility(View.VISIBLE);
            }
            GridLayout monthlyGrid = findViewById(R.id.monthlyCalendarGrid);
            if (selectedSpeciesButton != null) {
                helper.colorCalendarSquares(
                        monthlyGrid,
                        selectedSpeciesButton,
                        R.id.location_nav_krastaca_btn,
                        R.id.location_zel_rega_btn,
                        R.id.location_sekulja_btn,
                        CURRENT_MONTH
                );
            } else {
                helper.populateCalendar(monthlyGrid, CURRENT_MONTH, this::showSpeciesOverlay);
                disableFutureDates(monthlyGrid);
                updateCalendarColors();
            }
        } else {
            if (calendarGrid != null) {
                calendarGrid.setVisibility(View.GONE);
            }
            if (barGraphContainer != null) {
                barGraphContainer.setVisibility(View.VISIBLE);
                updateBarGraphs();
            }
        }
    }
    private void updateCalendarColors() {
        if (selectedSpeciesButton == null) {
            GridLayout monthlyGrid = findViewById(R.id.monthlyCalendarGrid);
            if (monthlyGrid != null) {
                helper.colorCalendarSquares(
                        monthlyGrid,
                        null,
                        R.id.location_nav_krastaca_btn,
                        R.id.location_zel_rega_btn,
                        R.id.location_sekulja_btn,
                        CURRENT_MONTH
                );
            }
        }
    }
    private void updateBarGraphs() {
        LinearLayout barGraphContainer = findViewById(R.id.graphContainer);
        if (barGraphContainer != null) {
            Log.d("SecondActivity", "updateBarGraphs: Calling populate with filter " + currentFilter);
            if (currentFilter == TimeFilter.MONTHLY) {
                helper.populateBarGraphs(
                        barGraphContainer,
                        selectedSpeciesButton,
                        R.id.location_nav_krastaca_btn,
                        R.id.location_zel_rega_btn,
                        R.id.location_sekulja_btn,
                        CURRENT_MONTH,
                        currentFilter
                );
            } else {
                helper.populateDailyAndWeeklyBarGraphs(
                        barGraphContainer,
                        selectedSpeciesButton,
                        R.id.location_nav_krastaca_btn,
                        R.id.location_zel_rega_btn,
                        R.id.location_sekulja_btn,
                        CURRENT_MONTH,
                        currentFilter
                );
            }
            helper.highlightTodayLabel(barGraphContainer, CURRENT_MONTH, currentFilter);
        }
    }
    private void handleSpeciesButtonClick(Button clickedButton, Button[] allButtons) {
        Log.d("SecondActivity", "handleSpeciesButtonClick: Selected button " + clickedButton.getText());
        if (selectedSpeciesButton == clickedButton) {
            selectedSpeciesButton = null;
            Log.d("SecondActivity", "Deselected species");
        } else {
            selectedSpeciesButton = clickedButton;
            Log.d("SecondActivity", "Selected species: " + clickedButton.getText());
        }

        highlightSelectedSpeciesButton(allButtons);
        if (currentFilter == TimeFilter.MONTHLY) {
            updateViewForFilter();
        } else {
            updateBarGraphs();
        }
    }
    private void handleTimeFilterButtonClick(Button clickedButton, TimeFilter filter) {
        Log.d("SecondActivity", "handleTimeFilterButtonClick: Changing filter to " + filter);
        if (currentFilter == filter) {
            currentFilter = TimeFilter.TODAY;
            Log.d("SecondActivity", "Filter reset to TODAY");
        } else {
            currentFilter = filter;
            Log.d("SecondActivity", "Filter set to " + filter);
        }

        updateViewForFilter();
    }
    private void highlightSelectedSpeciesButton(Button[] allButtons) {
        int selectedColor = ContextCompat.getColor(this, R.color.light_green);
        int defaultColor = ContextCompat.getColor(this, R.color.default_button_bg);

        for (Button btn : allButtons) {
            if (btn != null) {
                btn.setBackgroundColor(btn == selectedSpeciesButton ? selectedColor : defaultColor);
            }
        }
    }
    private void highlightSelectedFilterButton(Button today, Button weekly, Button monthly) {
        int selectedColor = ContextCompat.getColor(this, R.color.light_green);
        int defaultColor = ContextCompat.getColor(this, R.color.default_button_bg);

        Button[] buttons = { today, weekly, monthly };
        for (Button btn : buttons) {
            if (btn != null) {
                boolean isSelected =
                        (btn == today && currentFilter == TimeFilter.TODAY) ||
                                (btn == weekly && currentFilter == TimeFilter.WEEKLY) ||
                                (btn == monthly && currentFilter == TimeFilter.MONTHLY);
                btn.setBackgroundColor(isSelected ? selectedColor : defaultColor);
            }
        }
    }

    // Loudest frog section
    private void showSoundOverlayAndPlayVideo() {
        if (soundOverlay != null) {
            soundOverlay.setVisibility(View.VISIBLE);
        }

        if (videoWaveform != null) {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.no_bckgrnd_2);
            videoWaveform.setVideoURI(videoUri);

            videoWaveform.setOnPreparedListener(mp -> {
                if (duration != null) {
                    duration.setText(formatTime(mp.getDuration()));
                }
            });

            videoWaveform.start();

            if (btnPlay != null) {
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                updateTimer();
            }
        }
    }
    private void video() {
        if (videoWaveform != null) {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.no_bckgrnd_2);
            videoWaveform.setVideoURI(videoUri);

            videoWaveform.setOnPreparedListener(mp -> {
                if (duration != null) {
                    duration.setText(formatTime(mp.getDuration()));
                }
                soundOverlay.setVisibility(View.VISIBLE);
            });

            videoWaveform.setOnCompletionListener(mp -> {
                soundOverlay.setVisibility(View.GONE);
            });

            videoWaveform.start();
        }
    }

    // Donut section
    @SuppressLint("SetTextI18n")
    public void updateDonutData() {
        DonutView donutView = findViewById(R.id.donutView);
        if (donutView == null) return;

        LinearLayout parentLayout = findViewById(R.id.donutViewContainer);
        if (parentLayout == null) {
            parentLayout = findParentLinearLayout(donutView);
        }
        if (parentLayout == null) return;

        LinearLayout donutTable = findViewById(R.id.donutTable);
        if (donutTable != null) {
            parentLayout.removeView(donutTable);
        }

        donutTable = new LinearLayout(this);
        donutTable.setId(View.generateViewId());
        donutTable.setOrientation(LinearLayout.VERTICAL);
        donutTable.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        donutTable.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);

        Random random = new Random();
        float[] percentages = new float[3];
        int[] counts = new int[3];
        int totalCount = 0;

        for (int i = 0; i < 3; i++) {
            counts[i] = 30 + random.nextInt(71);
            totalCount += counts[i];
        }

        for (int i = 0; i < 3; i++) {
            percentages[i] = (counts[i] * 100.0f) / totalCount;
        }

        LinearLayout templateRow = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.donut_template, donutTable, false);
        updateRow(templateRow, 0, percentages[0], counts[0]);
        donutTable.addView(templateRow);

        for (int i = 1; i < 3; i++) {
            LinearLayout newRow = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.donut_template, donutTable, false);
            updateRow(newRow, i, percentages[i], counts[i]);
            donutTable.addView(newRow);
        }

        int donutViewIndex = parentLayout.indexOfChild(donutView);
        parentLayout.addView(donutTable, donutViewIndex + 1);

        donutView.setData(percentages);
    }
    private void updateRow(LinearLayout row, int index, float percentage, int count) {
        View colorDot = row.findViewById(R.id.colorDot);
        TextView speciesName = row.findViewById(R.id.speciesName);
        TextView speciesCount = row.findViewById(R.id.speciesCount);
        TextView speciesPercentage = row.findViewById(R.id.speciesPercentage);

        if (colorDot != null) {
            colorDot.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, DOT_COLORS[index])));
        }
        if (speciesName != null) {
            speciesName.setText(SPECIES_NAMES[index]);
        }
        if (speciesCount != null) {
            speciesCount.setText(String.valueOf(count));
        }
        if (speciesPercentage != null) {
            speciesPercentage.setText(String.format("%.1f%%", percentage));
        }
    }
    private LinearLayout findParentLinearLayout(View view) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent instanceof LinearLayout) {
                return (LinearLayout) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    // Helper methods
    @SuppressLint("DefaultLocale")
    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    private void updateTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (videoWaveform != null && videoWaveform.isPlaying()) {
                    int currentPosition = videoWaveform.getCurrentPosition();
                    if (timer != null) {
                        timer.setText(formatTime(currentPosition));
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }
    @Override
    public void onBackPressed() {
        if (currentOverlay != null) {
            if (currentOverlay.getParent() != null) {
                ((ViewGroup) currentOverlay.getParent()).removeView(currentOverlay);
                currentOverlay = null;
            }
        } else {
            super.onBackPressed();
        }
    }
    private boolean isAfterToday(int day) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);


        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.YEAR, 2025);
        selectedDate.set(Calendar.MONTH, Calendar.MAY);
        selectedDate.set(Calendar.DAY_OF_MONTH, day);
        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        return selectedDate.after(today);
    }
    private void disableFutureDates(GridLayout monthlyGrid) {
        if (monthlyGrid == null) return;

        int childCount = monthlyGrid.getChildCount();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < childCount; i++) {
            View child = monthlyGrid.getChildAt(i);
            if (child instanceof TextView) {
                TextView dayView = (TextView) child;
                try {
                    int day = Integer.parseInt(dayView.getText().toString());
                    dayView.setBackgroundColor(Color.TRANSPARENT);
                    if (day > todayDay) {
                        dayView.setClickable(false);
                        dayView.setBackgroundColor(Color.LTGRAY);
                        dayView.setTextColor(Color.GRAY);
                    }
                } catch (NumberFormatException e) {
                    Log.w("SecondActivity", "Non-numeric day found: " + dayView.getText(), e);
                }
            }
        }
    }
}