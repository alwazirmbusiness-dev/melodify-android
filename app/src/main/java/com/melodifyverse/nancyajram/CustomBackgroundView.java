package com.melodifyverse.nancyajram;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomBackgroundView extends View {
    private Paint paint;
    private List<GradientData> gradients;
    private List<ValueAnimator> animators;
    private Random random;

    private int[] colors = {
            Color.parseColor("#FF00FF"), // Magenta
            Color.parseColor("#FF5733"), // Bright Orange
            Color.parseColor("#FFFF00"),  // Yellow
            Color.parseColor("#DAF7A6"),  // Light Green
            Color.parseColor("#072222")    // Dark Color
    };

    private static class GradientData {
        RadialGradient gradient;
        float centerX, centerY, radius;
        int[] colors;
        float[] positions;

        GradientData(RadialGradient gradient, float centerX, float centerY, float radius, int[] colors, float[] positions) {
            this.gradient = gradient;
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
            this.colors = colors;
            this.positions = positions;
        }
    }

    public CustomBackgroundView(Context context) {
        super(context);
        init();
    }

    public CustomBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setMaskFilter(new BlurMaskFilter(50, BlurMaskFilter.Blur.NORMAL)); // Add Blur Effect
        random = new Random();
        gradients = new ArrayList<>();
        animators = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createGradients();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK); // Background color

        for (GradientData data : gradients) {
            paint.setShader(data.gradient);
            canvas.drawCircle(data.centerX, data.centerY, data.radius, paint);
        }
    }

    private void createGradients() {
        gradients.clear();
        animators.clear();

        int numGradients = 4; // Multiple gradients for the "blurry random effect"
        for (int i = 0; i < numGradients; i++) {
            float centerX = getWidth() / 2f + randomOffset(100);
            float centerY = getHeight() / 2f + randomOffset(100);
            float radius = Math.max(100f, getWidth() / 4f + randomOffset(50));

            int[] gradientColors = getRandomColors();
            float[] positions = null;

            RadialGradient gradient = new RadialGradient(
                    centerX, centerY, radius,
                    gradientColors, positions,
                    Shader.TileMode.CLAMP
            );

            GradientData gradientData = new GradientData(gradient, centerX, centerY, radius, gradientColors, positions);
            gradients.add(gradientData);

            startAnimation(i);
        }
    }

    private void startAnimation(int index) {
        final GradientData data = gradients.get(index);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 2f);
        animator.setDuration(6000 + random.nextInt(4000)); // Slower transition
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new LinearInterpolator()); // Smooth transition

        animator.addUpdateListener(animation -> {
            updateGradient(animation.getAnimatedFraction(), data);
            postInvalidateOnAnimation(); // Optimized rendering
        });

        animators.add(animator);
        animator.start();
    }

    private void updateGradient(float fraction, GradientData data) {
        float maxRadius = Math.min(getWidth(), getHeight()) / 2f;
        float minRadius = maxRadius / 4f;
        float easedFraction = (float) (0.5f + 0.5f * Math.sin(Math.PI * fraction)); // Smooth sinusoidal movement

        float radius = minRadius + (maxRadius - minRadius) * easedFraction;

        float maxOffset = maxRadius / 6f; // Adjust for soft, organic movement
        float offsetX = (float) (maxOffset * Math.cos(2 * Math.PI * fraction));
        float offsetY = (float) (maxOffset * Math.sin(2 * Math.PI * fraction));

        data.gradient = new RadialGradient(
                data.centerX + offsetX,
                data.centerY + offsetY,
                radius,
                data.colors,
                data.positions,
                Shader.TileMode.CLAMP
        );

        data.radius = radius;
    }

    private int[] getRandomColors() {
        int numColors = 5; // More colors for a glowing effect
        int[] colorsArray = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            colorsArray[i] = this.colors[random.nextInt(this.colors.length)];
        }
        return colorsArray;
    }

    private float randomOffset(float maxOffset) {
        return (random.nextFloat() * 2 - 1) * maxOffset;
    }
}
