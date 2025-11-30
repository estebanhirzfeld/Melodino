package com.example.melodino.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class VisualizerView extends View {

    private static final int BAR_COUNT = 40;
    private Paint paint;
    private float phase = 0;
    private boolean isPlaying = false;

    // Gradient Colors: Cyan -> Blue -> Purple -> Pink -> Green -> Cyan
    private final int[] colors = {
            0xFF00E5FF, // Cyan
            0xFF2979FF, // Blue
            0xFFAA00FF, // Purple
            0xFFFF4081, // Pink
            0xFF00E676, // Green
            0xFF00E5FF // Cyan
    };

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Create a linear gradient spanning the width of the view
        paint.setShader(new LinearGradient(0, 0, w, 0, colors, null, Shader.TileMode.CLAMP));
        // Bar width is total width / count, but we leave some gap (e.g., 60% bar, 40%
        // gap)
        paint.setStrokeWidth((w / (float) BAR_COUNT) * 0.6f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float barWidth = width / BAR_COUNT;

        for (int i = 0; i < BAR_COUNT; i++) {
            float actualHeight;

            if (isPlaying) {
                // Calculate organic height using sine waves
                // We combine multiple sine waves to create a more complex, "organic" feel

                // Wave 1: Main slow wave
                float h1 = (float) Math.sin(phase + i * 0.15f);

                // Wave 2: Faster, smaller wave
                float h2 = (float) Math.sin(phase * 2.2f + i * 0.4f);

                // Wave 3: Very fast, tiny jitter
                float h3 = (float) Math.sin(phase * 4.5f + i * 0.8f);

                // Combine and normalize roughly to 0..1 range
                // h1 is main (-1 to 1), h2 is detail (-1 to 1) scaled by 0.5, h3 scaled by 0.2
                float rawHeight = h1 + h2 * 0.5f + h3 * 0.2f;
                // Max possible value approx 1 + 0.5 + 0.2 = 1.7. Min -1.7.

                // Normalize to 0..1
                float normHeight = (rawHeight + 1.7f) / 3.4f;

                // Apply a window function (Hanning/Hamming-like) to taper edges if desired,
                // or just let it flow. The user's image shows tapering at ends.
                // Let's apply a simple bell curve factor based on index
                float xNorm = (float) i / BAR_COUNT; // 0 to 1
                float window = (float) Math.sin(xNorm * Math.PI); // 0 at ends, 1 in center

                // Final height calculation
                // Min height 10%, Max height 90%
                actualHeight = (height * 0.1f) + (height * 0.8f) * normHeight * window;

                // Ensure minimum height for visibility
                actualHeight = Math.max(actualHeight, height * 0.05f);
            } else {
                // Flat line (or very subtle wave) when idle
                // Let's make it a very thin, slightly undulating line to show it's "alive" but
                // waiting
                float idleWave = (float) Math.sin(phase * 0.5f + i * 0.1f) * (height * 0.02f);
                actualHeight = (height * 0.02f) + idleWave;
                actualHeight = Math.max(actualHeight, 4f); // Minimum 4px thickness
            }

            // Center vertically
            float startY = (height - actualHeight) / 2;
            float endY = startY + actualHeight;
            float x = i * barWidth + barWidth / 2;

            canvas.drawLine(x, startY, x, endY, paint);
        }

        // Always animate slightly to keep it alive
        phase += isPlaying ? 0.08f : 0.02f;
        postInvalidateOnAnimation();
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        // No need to postInvalidate here as we are always animating in onDraw
    }
}
