package pk_tnuv_mis.zaiba;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class PieChartView extends View {
    private float[] values;
    private String[] labels;
    private int[] colors;
    private Paint paint;
    private RectF rectF;
    private float total;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        rectF = new RectF();
        setClickable(true);
    }

    public void setData(float[] values, String[] labels) {
        this.values = values;
        this.labels = labels;
        this.total = 0;
        for (float value : values) {
            this.total += value;
        }

        colors = new int[]{
                ContextCompat.getColor(getContext(), R.color.light_green),
                ContextCompat.getColor(getContext(), R.color.dark_green_bar),
                Color.parseColor("#90EE90")
        };

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float size = Math.min(w, h) * 0.8f; // 80% of the smaller dimension
        float centerX = w / 2f;
        float centerY = h / 2f;
        rectF.set(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values == null || labels == null) return;

        float startAngle = 0;
        for (int i = 0; i < values.length; i++) {
            paint.setColor(colors[i % colors.length]);
            float sweepAngle = (values[i] / total) * 360;
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            double angle = Math.toDegrees(Math.atan2(y - centerY, x - centerX));
            if (angle < 0) angle += 360;

            float startAngle = 0;
            for (int i = 0; i < values.length; i++) {
                float sweepAngle = (values[i] / total) * 360;
                if (angle >= startAngle && angle < startAngle + sweepAngle) {
                    Toast.makeText(getContext(), labels[i] + ": " + (int)values[i], Toast.LENGTH_SHORT).show();
                    return true;
                }
                startAngle += sweepAngle;
            }
        }
        return super.onTouchEvent(event);
    }
}