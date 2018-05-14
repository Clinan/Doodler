package cn.edu.hdu.doodler.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by arter on 2018/4/28.
 */

public class ColorCircleView extends View {
//    public ColorCircleView(Context context) {
//        super(context);
//    }
//
//    public ColorCircleView(Context context, @Nullable AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public ColorCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init();
//    }
//
//    private void init() {
//
//    }

    private final float[] hsv;
    private float radius;
    private Paint mHSVCirclePaint;
    private Paint mChooserPaint;
    private RectF mRect;
    private float mHueChooserX;
    private float mHueChooserY;
    private float mSatChooserX;
    private float mSatChooserY;
    private float mValChooserX;
    private float mValChooserY;
    private float mChooserSize;
    private int mChooserMovable;
    private Bitmap mHSVBitmap;
    private int mAlpha;
    private ColorCircleView.OnColorChangeListener listener;
    private float FATIA;

    public ColorCircleView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ColorCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.hsv = new float[]{0.0F, 1.0F, 1.0F};
        this.radius = 0.0F;
        this.mChooserMovable = 0;
        this.mAlpha = 255;
        this.FATIA = 0.25F;
        this.init(attrs);
    }

    public static int getContrastColor(int color) {
        double y = (double) ((255 * Color.red(color) + 255 * Color.green(color) + 255 * Color.blue(color)) / 1000);
        return y >= 128.0D ? -16777216 : -1;
    }

    private void init(AttributeSet attrs) {
        this.mRect = new RectF();
        this.mHSVCirclePaint = new Paint();
        this.mHSVCirclePaint.setStrokeWidth(2.0F);
        this.mHSVCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        this.mHSVCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mHSVCirclePaint.setDither(true);
        this.mChooserPaint = new Paint(1);
        this.mChooserPaint.setStrokeWidth(1.0F);
        this.mChooserPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mHueChooserX = 3.5F * this.FATIA;
        this.mHueChooserY = 0.0F;
        this.mSatChooserX = 2.5F * this.FATIA;
        this.mSatChooserY = 0.0F;
        this.mValChooserX = 1.5F * this.FATIA;
        this.mValChooserY = 0.0F;
    }

    private void drawCircleView(Canvas canvas) {
        float size = this.radius * 2.0F;
        float[] aux = new float[]{this.hsv[0], this.hsv[1], this.hsv[2]};
        RectF rectF = new RectF(0.0F, 0.0F, size, size);

        int i;
        for (i = 0; i < 360; ++i) {
            aux[0] = (float) i;
            this.mHSVCirclePaint.setColor(Color.HSVToColor(this.mAlpha, aux));
            canvas.drawArc(rectF, (float) i, 1.0F, true, this.mHSVCirclePaint);
        }

        rectF.set(this.FATIA * this.radius, this.FATIA * this.radius, size - this.FATIA * this.radius, size - this.FATIA * this.radius);
        aux[0] = this.hsv[0];
        aux[2] = this.hsv[2];

        for (i = 0; i < 360; ++i) {
            aux[1] = (float) i / 360.0F;
            this.mHSVCirclePaint.setColor(Color.HSVToColor(this.mAlpha, aux));
            canvas.drawArc(rectF, (float) i, 1.0F, true, this.mHSVCirclePaint);
        }

        rectF.set(2.0F * this.FATIA * this.radius, 2.0F * this.FATIA * this.radius, size - 2.0F * this.FATIA * this.radius, size - 2.0F * this.FATIA * this.radius);
        aux[0] = this.hsv[0];
        aux[1] = this.hsv[1];

        for (i = 0; i < 360; ++i) {
            aux[2] = (float) i / 360.0F;
            this.mHSVCirclePaint.setColor(Color.HSVToColor(this.mAlpha, aux));
            canvas.drawArc(rectF, (float) i, 1.0F, true, this.mHSVCirclePaint);
        }

        rectF.set(3.0F * this.FATIA * this.radius, 3.0F * this.FATIA * this.radius, size - 3.0F * this.FATIA * this.radius, size - 3.0F * this.FATIA * this.radius);
        this.mHSVCirclePaint.setColor(Color.HSVToColor(this.mAlpha, this.hsv));
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), this.FATIA * this.radius, this.mHSVCirclePaint);
    }

    public void setOnColorChangeListener(ColorCircleView.OnColorChangeListener listener) {
        this.listener = listener;
    }

    public float getHue() {
        return this.hsv[0];
    }

    public void setHue(float value) {
        this.hsv[0] = Math.abs(value) % 360.0F;
        this.mHueChooserX = 3.5F * this.FATIA * (float) Math.cos(Math.toRadians((double) this.getHue()));
        this.mHueChooserY = 3.5F * this.FATIA * (float) Math.sin(Math.toRadians((double) this.getHue()));
        this.invalidate();
        this.requestLayout();
        this.fireOnColorChangeListener();
    }

    public float getSaturation() {
        return this.hsv[1];
    }

    public void setSaturation(float value) {
        this.hsv[1] = Math.min(1.0F, Math.max(0.0F, value));
        this.mSatChooserX = (float) Math.cos((double) (this.getSaturation() * 2.0F) * 3.141592653589793D) * 2.5F * this.FATIA;
        this.mSatChooserY = (float) Math.sin((double) (this.getSaturation() * 2.0F) * 3.141592653589793D) * 2.5F * this.FATIA;
        this.invalidate();
        this.requestLayout();
        this.fireOnColorChangeListener();
    }

    public float getValue() {
        return this.hsv[2];
    }

    public void setValue(float value) {
        this.hsv[2] = Math.min(1.0F, Math.max(0.0F, value));
        this.mValChooserX = (float) Math.cos((double) (this.getValue() * 2.0F) * 3.141592653589793D) * 1.5F * this.FATIA;
        this.mValChooserY = (float) Math.sin((double) (this.getValue() * 2.0F) * 3.141592653589793D) * 1.5F * this.FATIA;
        this.invalidate();
        this.requestLayout();
        this.fireOnColorChangeListener();
    }

    public int getColor() {
        return Color.HSVToColor(this.mAlpha, this.hsv);
    }

    public void setColor(int color) {
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), this.hsv);
        this.mHueChooserX = 3.5F * this.FATIA * (float) Math.cos(Math.toRadians((double) this.getHue()));
        this.mHueChooserY = 3.5F * this.FATIA * (float) Math.sin(Math.toRadians((double) this.getHue()));
        this.mSatChooserX = (float) Math.cos((double) (this.getSaturation() * 2.0F) * 3.141592653589793D) * 2.5F * this.FATIA;
        this.mSatChooserY = (float) Math.sin((double) (this.getSaturation() * 2.0F) * 3.141592653589793D) * 2.5F * this.FATIA;
        this.mValChooserX = (float) Math.cos((double) (this.getValue() * 2.0F) * 3.141592653589793D) * 1.5F * this.FATIA;
        this.mValChooserY = (float) Math.sin((double) (this.getValue() * 2.0F) * 3.141592653589793D) * 1.5F * this.FATIA;
        this.invalidate();
        this.requestLayout();
        this.fireOnColorChangeListener();
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        float d = 0.0F;
        float rad = 0.0F;
        switch (action) {
            case 0:
                d = this.getDistance(x - this.mRect.centerX(), y - this.mRect.centerY());
                rad = (float) ((Math.atan2((double) (-this.mRect.centerY() + y), (double) (-this.mRect.centerX() + x)) + 6.283185307179586D) % 6.283185307179586D);
                if (d >= 3.0F * this.FATIA * this.radius && d <= this.radius) {
                    this.setHue((float) Math.toDegrees((double) rad));
                } else if (d >= 2.0F * this.radius * this.FATIA && d < 3.0F * this.FATIA * this.radius) {
                    this.setSaturation(rad / 6.2831855F);
                } else if (d >= this.radius * this.FATIA && d < this.radius * 2.0F * this.FATIA) {
                    this.setValue(rad / 6.2831855F);
                }
            default:
                return true;
        }
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(this.mRect.centerX() - this.radius, this.mRect.centerY() - this.radius);
        this.drawCircleView(canvas);
        canvas.restore();
        int rgbColor = getContrastColor(Color.HSVToColor(this.hsv));
        this.mChooserPaint.setStyle(Paint.Style.FILL);
        this.mChooserPaint.setColor(rgbColor & 16777215 | 2130706432);
//        canvas.drawCircle(this.getChooserX(this.mHueChooserX), this.getChooserY(this.mHueChooserY), this.mChooserSize, this.mChooserPaint);
//        canvas.drawCircle(this.getChooserX(this.mSatChooserX), this.getChooserY(this.mSatChooserY), this.mChooserSize, this.mChooserPaint);
        canvas.drawCircle(this.getChooserX(this.mValChooserX), this.getChooserY(this.mValChooserY), this.mChooserSize, this.mChooserPaint);
        this.mChooserPaint.setStyle(Paint.Style.FILL);
        this.mChooserPaint.setColor(rgbColor);
//        canvas.drawCircle(this.getChooserX(this.mHueChooserX), this.getChooserY(this.mHueChooserY), 3.0F, this.mChooserPaint);
//        canvas.drawCircle(this.getChooserX(this.mSatChooserX), this.getChooserY(this.mSatChooserY), 3.0F, this.mChooserPaint);
        canvas.drawCircle(this.getChooserX(this.mValChooserX), this.getChooserY(this.mValChooserY), 3.0F, this.mChooserPaint);
    }

    private float getChooserX(float x) {
        return this.mRect.centerX() + x * this.radius;
    }

    private float getChooserY(float y) {
        return this.mRect.centerY() + y * this.radius;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float a = (float) (Math.min(this.getWidth(), this.getHeight()) - 5);
        this.radius = a / 2.0F;
        this.mChooserSize = this.FATIA * this.radius / 3.0F;
        float rectLeft = (float) (this.getWidth() / 2) - this.radius + 1.0F;
        float rectTop = (float) (this.getHeight() / 2) - this.radius + 1.0F;
        float rectRight = (float) (this.getWidth() / 2) - this.radius + a - 1.0F;
        float rectBottom = (float) (this.getHeight() / 2) - this.radius + a - 1.0F;
        this.mRect.set(rectLeft, rectTop, rectRight, rectBottom);
        --this.radius;
    }

    private boolean insideInHueChooser(float x, float y) {
        return Math.pow((double) (x - this.getChooserX(this.mHueChooserX)), 2.0D) + Math.pow((double) (y - this.getChooserY(this.mHueChooserY)), 2.0D) <= (double) (this.mChooserSize * this.mChooserSize);
    }

    private boolean insideInSatChooser(float x, float y) {
        return Math.pow((double) (x - this.getChooserX(this.mSatChooserX)), 2.0D) + Math.pow((double) (y - this.getChooserY(this.mSatChooserY)), 2.0D) <= (double) (this.mChooserSize * this.mChooserSize);
    }

    private boolean insideInValChooser(float x, float y) {
        return Math.pow((double) (x - this.getChooserX(this.mValChooserX)), 2.0D) + Math.pow((double) (y - this.getChooserY(this.mValChooserY)), 2.0D) <= (double) (this.mChooserSize * this.mChooserSize);
    }

    private float getDistance(float x, float y) {
        return (float) Math.sqrt(Math.pow((double) x, 2.0D) + Math.pow((double) y, 2.0D));
    }

    private void fireOnColorChangeListener() {
        if (this.listener != null) {
            this.listener.onColorChanged(this);
        }

    }

    public interface OnColorChangeListener {
        void onColorChanged(ColorCircleView var1);
    }
}
