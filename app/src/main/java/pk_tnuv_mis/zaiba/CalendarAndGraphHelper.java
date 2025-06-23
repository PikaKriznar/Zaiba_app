package pk_tnuv_mis.zaiba;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class CalendarAndGraphHelper {
    private final Context context;

    public CalendarAndGraphHelper(Context context) {
        this.context = context;
    }

    public void populateCalendar(GridLayout calendarGrid, String month, Consumer<Integer> dayClickListener) {
        calendarGrid.removeAllViews();

        int numberOfDays = getDaysInMonth(month);
        int firstDayOfWeek = getFirstDayOfMonth(month);
        int paddingDays = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - Calendar.MONDAY;

        for (int i = 0; i < paddingDays; i++) {
            FrameLayout container = new FrameLayout(context);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            container.setLayoutParams(params);
            container.setPadding(4, 4, 4, 4);

            View background = new View(context);
            background.setId(View.generateViewId());
            background.setBackgroundColor(Color.WHITE);
            background.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            background.setVisibility(View.INVISIBLE);
            container.addView(background);

            calendarGrid.addView(container);
        }

        for (int day = 1; day <= numberOfDays; day++) {
            FrameLayout container = new FrameLayout(context);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            container.setLayoutParams(params);
            container.setPadding(4, 4, 4, 4);

            View background = new View(context);
            background.setId(View.generateViewId());
            background.setBackgroundColor(Color.WHITE);
            background.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            container.addView(background);

            TextView dayCell = new TextView(context);
            dayCell.setId(View.generateViewId());
            dayCell.setText(String.valueOf(day));
            dayCell.setGravity(Gravity.CENTER);
            dayCell.setTextColor(Color.BLACK);
            dayCell.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
            ));

            int finalDay = day;
            dayCell.setOnClickListener(v -> {
                View bgView = ((FrameLayout) v.getParent()).getChildAt(0);
                if (dayClickListener != null) {
                    dayClickListener.accept(finalDay);
                }
            });

            container.addView(dayCell);
            calendarGrid.addView(container);
        }
    }
    public void colorCalendarSquares(GridLayout calendarGrid, Button selectedSpeciesButton, int speciesAId, int speciesBId, int speciesCId, String month) {
        try {
            String jsonFile = getJsonFileForMonth(month);
            InputStream is = context.getAssets().open(jsonFile);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(json);
            JSONObject monthData = jsonObject.getJSONObject(month);

            int lightGreen = ContextCompat.getColor(context, R.color.light_green);
            int darkGreen = ContextCompat.getColor(context, R.color.g1);
            Calendar today = Calendar.getInstance();
            int todayDay = today.get(Calendar.DAY_OF_MONTH);
            int todayMonth = today.get(Calendar.MONTH);
            int todayYear = today.get(Calendar.YEAR);
            boolean isCurrentMonth = month.equals("May 2025") && todayYear == 2025 && todayMonth == Calendar.MAY;

            int rows = calendarGrid.getRowCount();
            int cols = calendarGrid.getColumnCount();
            int cellIndex = 0;

            for (int row = rows - 1; row >= 0; row--) {
                for (int col = 0; col < cols; col++) {
                    if (cellIndex >= calendarGrid.getChildCount()) {
                        break;
                    }

                    FrameLayout container = (FrameLayout) calendarGrid.getChildAt(cellIndex);
                    View background = container.getChildAt(0);
                    TextView dayText = (TextView) container.getChildAt(1);

                    if (dayText != null && background.getVisibility() != View.INVISIBLE) {
                        int day = Integer.parseInt(dayText.getText().toString());

                        if (isCurrentMonth && day > todayDay) {
                            background.setBackgroundColor(Color.WHITE);
                            cellIndex++;
                            continue;
                        }

                        String dayKey = String.valueOf(day);
                        if (monthData.has(dayKey)) {
                            JSONObject dayData = monthData.getJSONObject(dayKey);

                            int count;
                            if (selectedSpeciesButton != null) {
                                int selectedId = selectedSpeciesButton.getId();
                                if (selectedId == speciesAId) {
                                    count = dayData.getInt("navadna krastača");
                                } else if (selectedId == speciesBId) {
                                    count = dayData.getInt("zelena rega");
                                } else if (selectedId == speciesCId) {
                                    count = dayData.getInt("sekulja");
                                } else {
                                    count = dayData.getInt("total");
                                }
                            } else {
                                count = dayData.getInt("total");
                            }

                            float fillFraction = (count - 20f) / (200f - 20f);
                            fillFraction = Math.max(0f, Math.min(1f, fillFraction));

                            ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
                            int fillColor = (isCurrentMonth && day == todayDay) ? darkGreen : lightGreen;
                            LinearGradient gradient = new LinearGradient(
                                    0, 100, 0, 100 * (1 - fillFraction),
                                    new int[]{fillColor, fillColor, Color.WHITE},
                                    new float[]{0, fillFraction, fillFraction},
                                    Shader.TileMode.CLAMP
                            );
                            shapeDrawable.getPaint().setShader(gradient);
                            background.setBackground(shapeDrawable);
                        }
                    }

                    cellIndex++;
                }
            }
        } catch (Exception e) {
            Log.e("CalendarAndGraphHelper", "Error reading or parsing JSON for " + month, e);
        }
    }
    public void populateBarGraphs(LinearLayout graphContainer, Button selectedSpeciesButton, int speciesAId, int speciesBId, int speciesCId, String month, SecondActivity.TimeFilter filter) {
        try {
            Log.d("CalendarAndGraphHelper", "populateBarGraphs called with filter: " + filter);
            String jsonFile = getJsonFileForMonth(month);
            InputStream is = context.getAssets().open(jsonFile);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            JSONObject monthData = jsonObject.getJSONObject(month);

            Calendar today = Calendar.getInstance();
            int todayDay = today.get(Calendar.DAY_OF_MONTH);
            int todayMonth = today.get(Calendar.MONTH);
            int todayYear = today.get(Calendar.YEAR);
            boolean isCurrentMonth = month.equals("May 2025") && todayYear == 2025 && todayMonth == Calendar.MAY;

            int numberOfDays = getDaysInMonth(month);
            int childCount = Math.min(graphContainer.getChildCount(), numberOfDays);

            Log.d("CalendarAndGraphHelper", "Monthly: Processing " + childCount + " days, todayDay: " + todayDay);
            for (int i = 0; i < childCount; i++) {
                View dayColumn = graphContainer.getChildAt(i);
                if (!(dayColumn instanceof LinearLayout)) {
                    Log.w("CalendarAndGraphHelper", "Child at " + i + " is not LinearLayout");
                    continue;
                }

                LinearLayout column = (LinearLayout) dayColumn;
                View firstChild = column.getChildAt(0);
                if (!(firstChild instanceof LinearLayout)) {
                    Log.w("CalendarAndGraphHelper", "First child at " + i + " is not LinearLayout");
                    continue;
                }

                LinearLayout bar = (LinearLayout) firstChild;
                TextView label = (TextView) column.getChildAt(1);

                int day = i + 1;
                if (isCurrentMonth && day > todayDay) {
                    bar.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 0));
                    label.setTextColor(ContextCompat.getColor(context, R.color.black));
                    Log.d("CalendarAndGraphHelper", "Skipping future day: " + day);
                    continue;
                }

                String dayKey = String.valueOf(day);
                int height = 0;
                int barColor = ContextCompat.getColor(context, R.color.light_green);
                int count = 0;

                if (monthData.has(dayKey)) {
                    JSONObject dayData = monthData.getJSONObject(dayKey);
                    if (selectedSpeciesButton != null) {
                        int selectedId = selectedSpeciesButton.getId();
                        if (selectedId == speciesAId) {
                            count = dayData.getInt("navadna krastača");
                        } else if (selectedId == speciesBId) {
                            count = dayData.getInt("zelena rega");
                        } else if (selectedId == speciesCId) {
                            count = dayData.getInt("sekulja");
                        } else {
                            count = dayData.getInt("total");
                        }
                        float heightFraction = (count - 10f) / (100f - 10f);
                        heightFraction = Math.max(0f, Math.min(1f, heightFraction));
                        height = (int) (heightFraction * 100) + 20;
                    } else {
                        count = dayData.getInt("total");
                        float heightFraction = (count - 30f) / (300f - 30f);
                        heightFraction = Math.max(0f, Math.min(1f, heightFraction));
                        height = (int) (heightFraction * 100) + 20;
                    }

                    if (isCurrentMonth && day == todayDay) {
                        barColor = ContextCompat.getColor(context, R.color.dark_green_bar);
                    }
                }

                bar.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, height));
                bar.setBackgroundColor(barColor);
                label.setText("Day " + day + "\n" + count);
                label.setTextColor(ContextCompat.getColor(context, isCurrentMonth && day == todayDay ?
                        R.color.dark_green_bar : R.color.black));
                Log.d("CalendarAndGraphHelper", "Day " + day + " count: " + count + ", height: " + height);
            }
        } catch (Exception e) {
            Log.e("CalendarAndGraphHelper", "Error reading or parsing JSON for bar graphs", e);
        }
    }
    public void populateDailyAndWeeklyBarGraphs(
            LinearLayout graphContainer,
            Button selectedSpeciesButton,
            int krastacaId,
            int regaId,
            int sekuljaId,
            String monthName,
            SecondActivity.TimeFilter filter) {

        try {
            InputStream is = context.getAssets().open(getJsonFileForMonth(monthName));
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONObject json = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            JSONObject monthData = json.getJSONObject(monthName);

            Calendar today = Calendar.getInstance();
            int todayDay = today.get(Calendar.DAY_OF_MONTH);
            int todayMonth = today.get(Calendar.MONTH);
            int todayYear = today.get(Calendar.YEAR);
            boolean isCurrentMonth = monthName.equals("May 2025") && todayYear == 2025 && todayMonth == Calendar.MAY;

            LinearLayout barGraphContainer = graphContainer.findViewById(R.id.barGraphContainer);
            FrameLayout pieChartContainer = graphContainer.findViewById(R.id.pieChartContainer);

            if (barGraphContainer == null || pieChartContainer == null) {
                Log.e("CalendarAndGraphHelper", "Could not find barGraphContainer or pieChartContainer");
                return;
            }

            int[] daysToShow;
            int expectedBars;
            if (filter == SecondActivity.TimeFilter.TODAY) {
                daysToShow = new int[]{todayDay};
                expectedBars = 3;
            } else {
                Calendar weekStart = (Calendar) today.clone();
                weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                int daysInWeek = (today.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7 + 1;
                daysToShow = new int[daysInWeek];
                for (int i = 0; i < daysInWeek; i++) {
                    daysToShow[i] = weekStart.get(Calendar.DAY_OF_MONTH);
                    weekStart.add(Calendar.DAY_OF_MONTH, 1);
                }
                expectedBars = daysInWeek;
            }

            int todayIndex = -1;
            if (filter == SecondActivity.TimeFilter.TODAY) {
                todayIndex = 0;
            } else {
                for (int i = 0; i < daysToShow.length; i++) {
                    if (daysToShow[i] == todayDay) {
                        todayIndex = i;
                        break;
                    }
                }
            }

            if (filter == SecondActivity.TimeFilter.TODAY) {
                barGraphContainer.setVisibility(View.GONE);
                pieChartContainer.setVisibility(View.VISIBLE);

                // Placeholder for pie chart logic (assuming PieChartView exists)
                PieChartView pieChartView;
                if (pieChartContainer.getChildCount() == 0) {
                    pieChartView = new PieChartView(context);
                    pieChartContainer.addView(pieChartView, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    ));
                } else {
                    pieChartView = (PieChartView) pieChartContainer.getChildAt(0);
                }

                String dayKey = String.valueOf(todayDay);
                if (monthData.has(dayKey)) {
                    JSONObject dayData = monthData.getJSONObject(dayKey);
                    float[] counts = new float[]{
                            dayData.getInt("navadna krastača"),
                            dayData.getInt("zelena rega"),
                            dayData.getInt("sekulja")
                    };
                    String[] labels = new String[]{"Navadna krastača", "Zelena rega", "Sekulja"};
                    pieChartView.setData(counts, labels);
                }
            } else {
                barGraphContainer.setVisibility(View.VISIBLE);
                pieChartContainer.setVisibility(View.GONE);

                int[] barIds = {
                        R.id.bar_monday, R.id.bar_tuesday, R.id.bar_wednesday,
                        R.id.bar_thursday, R.id.bar_friday, R.id.bar_saturday, R.id.bar_sunday
                };

                float scale = context.getResources().getDisplayMetrics().density;
                int maxHeightDp = 170;
                int maxHeightPx = (int) (maxHeightDp * scale);

                int childCount = Math.min(barGraphContainer.getChildCount(), expectedBars);
                Log.d("CalendarAndGraphHelper", "Processing " + childCount + " bars for Weekly, todayDay: " + todayDay);

                for (int i = 0; i < childCount; i++) {
                    LinearLayout column = (LinearLayout) barGraphContainer.getChildAt(i);
                    View bar = column.findViewById(barIds[i]);
                    TextView label = (TextView) column.getChildAt(1);

                    if (bar == null) {
                        Log.w("CalendarAndGraphHelper", "Bar not found for ID: " + barIds[i]);
                        continue;
                    }

                    int day = daysToShow[i];
                    if (isCurrentMonth && day > todayDay) {
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
                        params.height = 0;
                        bar.setLayoutParams(params);
                        label.setTextColor(ContextCompat.getColor(context, R.color.black));
                        Log.d("CalendarAndGraphHelper", "Skipping future day: " + day);
                        continue;
                    }

                    String dayKey = String.valueOf(day);
                    int heightPx = 0;
                    int barColor = ContextCompat.getColor(context, R.color.light_green);
                    int count = 0;

                    if (monthData.has(dayKey)) {
                        JSONObject dayData = monthData.getJSONObject(dayKey);
                        if (selectedSpeciesButton != null) {
                            int selectedId = selectedSpeciesButton.getId();
                            if (selectedId == krastacaId) {
                                count = dayData.getInt("navadna krastača");
                            } else if (selectedId == regaId) {
                                count = dayData.getInt("zelena rega");
                            } else if (selectedId == sekuljaId) {
                                count = dayData.getInt("sekulja");
                            } else {
                                count = dayData.getInt("total");
                            }
                            float heightFraction = (count - 10f) / (100f - 10f);
                            heightPx = (int) (heightFraction * maxHeightPx);
                        } else {
                            count = dayData.getInt("total");
                            float heightFraction = (count - 30f) / (300f - 30f);
                            heightPx = (int) (heightFraction * maxHeightPx);
                        }
                        heightPx = Math.min(maxHeightPx, Math.max(0, heightPx));

                        if (isCurrentMonth && i == todayIndex) {
                            barColor = ContextCompat.getColor(context, R.color.dark_green_bar);
                        }
                    }

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
                    params.height = heightPx;
                    bar.setLayoutParams(params);
                    bar.setBackgroundColor(barColor);
                    label.setText("Day " + day + "\n" + count);
                    label.setTextColor(ContextCompat.getColor(context, isCurrentMonth && i == todayIndex ?
                            R.color.dark_green_bar : R.color.black));
                    Log.d("CalendarAndGraphHelper", "Weekly bar " + i + ": Day " + day + " count: " + count + ", heightPx: " + heightPx);
                }
            }
        } catch (Exception e) {
            Log.e("CalendarAndGraphHelper", "Error reading or parsing JSON for bar graphs", e);
        }
    }
    public void highlightTodayLabel(LinearLayout graphContainer, String month, SecondActivity.TimeFilter filter) {
        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH);
        int todayYear = today.get(Calendar.YEAR);
        boolean isCurrentMonth = month.equals("May 2025") && todayYear == 2025 && todayMonth == Calendar.MAY;

        int[] daysToShow;
        int daysCount;
        if (filter == SecondActivity.TimeFilter.TODAY) {
            daysToShow = new int[]{todayDay};
            daysCount = 3;
        } else if (filter == SecondActivity.TimeFilter.WEEKLY) {
            Calendar weekStart = (Calendar) today.clone();
            weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            daysCount = (today.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7 + 1;
            daysToShow = new int[daysCount];
            for (int i = 0; i < daysCount; i++) {
                daysToShow[i] = weekStart.get(Calendar.DAY_OF_MONTH);
                weekStart.add(Calendar.DAY_OF_MONTH, 1);
            }
        } else {
            daysToShow = new int[getDaysInMonth(month)];
            for (int i = 0; i < daysToShow.length; i++) {
                daysToShow[i] = i + 1;
            }
            daysCount = daysToShow.length;
        }

        int todayIndex = -1;
        for (int i = 0; i < daysToShow.length; i++) {
            if (daysToShow[i] == todayDay) {
                todayIndex = i;
                break;
            }
        }

        if (graphContainer != null) {
            for (int i = 0; i < Math.min(graphContainer.getChildCount(), daysCount); i++) {
                LinearLayout dayLayout = (LinearLayout) graphContainer.getChildAt(i);
                if (dayLayout == null) continue;

                TextView dayLabel = (TextView) dayLayout.getChildAt(1);
                if (dayLabel == null) continue;

                if (filter == SecondActivity.TimeFilter.TODAY || (isCurrentMonth && i == todayIndex)) {
                    dayLabel.setTypeface(null, Typeface.BOLD);
                    dayLabel.setTextColor(ContextCompat.getColor(context, R.color.g3));
                } else {
                    dayLabel.setTypeface(null, Typeface.NORMAL);
                    dayLabel.setTextColor(ContextCompat.getColor(context, R.color.g3));
                }
                Log.d("CalendarAndGraphHelper", "Highlighting label at index " + i + ", todayIndex: " + todayIndex);
            }
        }
    }

    // Helper methods
    private int getDaysInMonth(String month) {
        switch (month) {
            case "May 2025":
                return 31;
            case "June 2025":
                return 30;
            case "April 2025":
            default:
                return 30;
        }
    }
    private int getFirstDayOfMonth(String month) {
        Calendar calendar = Calendar.getInstance();
        if (month.equals("May 2025")) {
            calendar.set(2025, Calendar.MAY, 1);
        } else if (month.equals("June 2025")) {
            calendar.set(2025, Calendar.JUNE, 1);
        } else {
            calendar.set(2025, Calendar.APRIL, 1);
        }
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
    public String getJsonFileForMonth(String month) {
        switch (month) {
            case "May 2025":
                return "may_data.json";
            case "June 2025":
                return "june_data.json";
            case "April 2025":
            default:
                return "april_data.json";
        }
    }
}