package com.core.talita;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom view for data visualization
 * Creates generative art based on user's data
 */
public class DataVisualizationView extends View {
    private Paint paint;
    private List<PersonalData> data;
    private float breathingScale = 1.0f;
    private int breathingSpeed = 2000;
    private ValueAnimator breathAnimator;
    
    // Particle system for data points
    private List<DataParticle> particles;
    private Random random = new Random();
    
    public DataVisualizationView(Context context) {
        super(context);
        init();
    }
    
    public DataVisualizationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DataVisualizationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        particles = new ArrayList<>();
        data = new ArrayList<>();
        
        // Initialize with some ambient particles
        for (int i = 0; i < 5; i++) {
            particles.add(new DataParticle());
        }
    }
    
    public void setData(List<PersonalData> newData) {
        this.data = newData;
        
        // Create particles for each data point
        particles.clear();
        for (PersonalData item : data) {
            DataParticle particle = new DataParticle();
            particle.setFromData(item);
            particles.add(particle);
        }
        
        // Add some ambient particles for visual interest
        int ambientCount = Math.max(5, 20 - data.size());
        for (int i = 0; i < ambientCount; i++) {
            particles.add(new DataParticle());
        }
        
        invalidate();
    }
    
    public void breathe() {
        if (breathAnimator != null && breathAnimator.isRunning()) {
            return;
        }
        
        breathAnimator = ValueAnimator.ofFloat(1.0f, 1.1f, 1.0f);
        breathAnimator.setDuration(breathingSpeed);
        breathAnimator.addUpdateListener(animation -> {
            breathingScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        breathAnimator.start();
    }
    
    public void setBreathingSpeed(int speed) {
        this.breathingSpeed = speed;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        // Draw background glow
        paint.setColor(Color.argb(20, 147, 112, 219)); // Soft purple glow
        float glowRadius = Math.min(centerX, centerY) * 0.8f * breathingScale;
        canvas.drawCircle(centerX, centerY, glowRadius, paint);
        
        // Draw particles
        for (DataParticle particle : particles) {
            particle.update();
            particle.draw(canvas, paint, centerX, centerY, breathingScale);
        }
        
        // Draw central orb
        paint.setColor(Color.argb(60, 147, 112, 219));
        float orbRadius = 80 * breathingScale;
        canvas.drawCircle(centerX, centerY, orbRadius, paint);
        
        paint.setColor(Color.argb(100, 147, 112, 219));
        canvas.drawCircle(centerX, centerY, orbRadius * 0.7f, paint);
        
        // Draw data count
        if (data != null && !data.isEmpty()) {
            paint.setColor(Color.WHITE);
            paint.setTextSize(48);
            paint.setTextAlign(Paint.Align.CENTER);
            String count = String.valueOf(data.size());
            canvas.drawText(count, centerX, centerY + 15, paint);
        }
        
        // Continuous animation
        invalidate();
    }
    
    /**
     * Data particle for visualization
     */
    private class DataParticle {
        float x, y;
        float vx, vy;
        float radius;
        int color;
        float alpha;
        float angle;
        float distance;
        float speed;
        
        DataParticle() {
            reset();
        }
        
        void reset() {
            angle = random.nextFloat() * 360;
            distance = 100 + random.nextFloat() * 200;
            speed = 0.1f + random.nextFloat() * 0.5f;
            radius = 5 + random.nextFloat() * 10;
            alpha = 0.3f + random.nextFloat() * 0.7f;
            color = Color.argb((int)(alpha * 255), 147, 112, 219);
        }
        
        void setFromData(PersonalData data) {
            // Customize particle based on data type
            String type = data.getDataType();
            
            if (type.contains("location")) {
                color = Color.argb(200, 100, 200, 255); // Blue for location
                radius = 12;
            } else if (type.contains("water")) {
                color = Color.argb(200, 100, 150, 255); // Light blue for water
                radius = 10;
            } else if (type.contains("mood")) {
                color = Color.argb(200, 255, 200, 100); // Yellow for mood
                radius = 15;
            } else if (type.contains("exercise")) {
                color = Color.argb(200, 255, 100, 100); // Red for exercise
                radius = 14;
            } else {
                color = Color.argb(200, 147, 112, 219); // Default purple
                radius = 10;
            }
            
            // Randomize position
            angle = random.nextFloat() * 360;
            distance = 150 + random.nextFloat() * 100;
            speed = 0.2f + random.nextFloat() * 0.3f;
        }
        
        void update() {
            angle += speed;
            if (angle > 360) angle -= 360;
            
            // Slight oscillation in distance
            distance += Math.sin(angle * 0.05) * 0.5;
        }
        
        void draw(Canvas canvas, Paint paint, int centerX, int centerY, float scale) {
            // Calculate position
            float radian = (float) Math.toRadians(angle);
            x = centerX + (float) Math.cos(radian) * distance * scale;
            y = centerY + (float) Math.sin(radian) * distance * scale;
            
            // Draw particle
            paint.setColor(color);
            canvas.drawCircle(x, y, radius * scale, paint);
            
            // Draw connection line to center
            paint.setColor(Color.argb(30, 147, 112, 219));
            paint.setStrokeWidth(1);
            canvas.drawLine(centerX, centerY, x, y, paint);
        }
    }
}
