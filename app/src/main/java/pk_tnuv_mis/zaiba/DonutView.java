package pk_tnuv_mis.zaiba;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class DonutView extends View {
    private Paint[] paints;
    private float[] percentages;
    private RectF[] bounds;
    private float ringThickness;
    private float radiusIncrement;
    private float baseRadius;

    public DonutView(Context context) {
        this(context, null);
    }

    public DonutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        float density = getResources().getDisplayMetrics().density;
        ringThickness = 10 * density;
        radiusIncrement = 5 * density;
        baseRadius = 50 * density;

        paints = new Paint[3];
        int[] colors = new int[]{
                ContextCompat.getColor(context, R.color.g3),
                ContextCompat.getColor(context, R.color.g4),
                ContextCompat.getColor(context, R.color.g5)
        };
        for (int i = 0; i < 3; i++) {
            paints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            paints[i].setStyle(Paint.Style.STROKE);
            paints[i].setStrokeWidth(ringThickness);
            paints[i].setColor(colors[i]);
        }

        percentages = new float[]{0f, 0f, 0f};
        bounds = new RectF[3];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float centerX = w / 2f;
        float centerY = h / 2f;
        for (int i = 0; i < 3; i++) {
            float radius = baseRadius + i * (ringThickness + radiusIncrement);
            float outerRadius = radius + ringThickness / 2;
            bounds[i] = new RectF(
                    centerX - outerRadius,
                    centerY - outerRadius,
                    centerX + outerRadius,
                    centerY + outerRadius
            );
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < 3; i++) {
            if (percentages[i] > 0) {
                float sweepAngle = (percentages[i] / 100f) * 360f;
                canvas.drawArc(bounds[i], -90, sweepAngle, false, paints[i]);
            }
        }
    }

    public void setData(float[] percentages) {
        if (percentages == null || percentages.length != 3) {
            this.percentages = new float[]{0f, 0f, 0f};
        } else {
            this.percentages = percentages.clone();
        }
        invalidate();
    }
}