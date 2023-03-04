package org.telegram.ui.Components.voip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;

public class EllipsizeView extends FrameLayout {

    private final int sizePx = AndroidUtilities.dp(20);
    private final int maxStrokeWidth = AndroidUtilities.dp(6);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    float animationProgress = 0f;

    private float initGap = 0.85f;

    public EllipsizeView(@NonNull Context context) {
        super(context);
        setWillNotDraw(false);
        dotPaint.setColor(Color.WHITE);
        dotPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(sizePx, maxStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int y = maxStrokeWidth / 2;
        int centerX = sizePx / 2;
        int x1 = calculateX(0, centerX * initGap, animationProgress);
        int strokeWidth1 = calculateStoke(0, maxStrokeWidth * initGap, animationProgress);
        //dotPaint.setStrokeWidth(strokeWidth1);
        //canvas.drawPoint(x1, y, dotPaint);
        canvas.drawCircle(x1, y, strokeWidth1 / 2f, dotPaint);

        int x2 = calculateX(centerX * initGap, sizePx * initGap, animationProgress);
        int strokeWidth2;
        if (x2 <= centerX) {
            strokeWidth2 = calculateStoke(maxStrokeWidth * initGap, maxStrokeWidth, animationProgress * (initGap / (1f - initGap)));
        } else {
            strokeWidth2 = calculateStoke(maxStrokeWidth * (2f - initGap * 2f), maxStrokeWidth, (1f - animationProgress) * (initGap / (2f * initGap - 1f)));
        }
        //dotPaint.setStrokeWidth(strokeWidth2);
        //canvas.drawPoint(x2, y, dotPaint);
        canvas.drawCircle(x2, y, strokeWidth2 / 2f, dotPaint);


        int x3 = calculateX(sizePx * initGap, sizePx * initGap * 1.5f, animationProgress);
        if (x3 < sizePx) {
            int strokeWidth3 = calculateStoke(0, maxStrokeWidth * (2f - initGap * 2f), 1f - animationProgress);
            //dotPaint.setStrokeWidth(strokeWidth3);
            //canvas.drawPoint(x3, y, dotPaint);
            canvas.drawCircle(x3, y, strokeWidth3 / 2f, dotPaint);
        }
    }

    int calculateX(float min, float max, float progress) {
        return (int) (min + progress * (max - min));
    }

    int calculateStoke(float min, float max, float progress) {
        return (int) (min + progress * (max - min));
    }
}
