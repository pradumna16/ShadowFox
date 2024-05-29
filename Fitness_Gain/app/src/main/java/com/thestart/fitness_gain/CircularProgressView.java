package com.thestart.fitness_gain;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircularProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;

    private float strokeWidth = 20f;
    private float progress = 0f;
    private float maxProgress = 100f;

    public CircularProgressView(Context context) {
        super(context);
        init(null);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(Color.BLUE);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircularProgressView);
            progress = a.getFloat(R.styleable.CircularProgressView_progress, progress);
            maxProgress = a.getFloat(R.styleable.CircularProgressView_maxProgress, maxProgress);
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - strokeWidth / 2f;

        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        float sweepAngle = (progress / maxProgress) * 360f;
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, -90f, sweepAngle, false, progressPaint);

        String progressText = String.format("%.0f%%", (progress / maxProgress) * 100);
        canvas.drawText(progressText, centerX, centerY + (textPaint.descent() - textPaint.ascent()) / 2 - textPaint.descent(), textPaint);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public float getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }
}
