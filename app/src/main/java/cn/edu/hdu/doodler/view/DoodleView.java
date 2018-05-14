package cn.edu.hdu.doodler.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;


public class DoodleView extends View {
    private static final String TAG = "DrawingView";
    private static final float TOUCH_TOLERANCE = 4;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private Paint mPenPaint;
    private float mPenSize = 2.0f;
    private int mPenAlpha = 255;
    private boolean mDrawMode;
    private float mX, mY;
    private @ColorInt
    int backgroupColor = Color.WHITE;
    private Bitmap mLoadBitmap;
    private Paint mEraserPaint;
    private float mEraserSize = 10.0f;
    private LinkedList<Bitmap> historyBitmaps;
    private LinkedList<Bitmap> removeBitmaps;
    private boolean isUndo = false;
    private boolean isRedo = false;

    public DoodleView(Context c) {
        this(c, null);
    }

    public DoodleView(Context c, AttributeSet attrs) {
        this(c, attrs, 0);
    }

    public DoodleView(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        init();
    }

    private void init() {
        Log.d(TAG, "init: ");
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mDrawMode = false;
        historyBitmaps = new LinkedList<>();
        removeBitmaps = new LinkedList<>();
        initializePen();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mBitmap != null) {
            if ((mBitmap.getHeight() > heightSize) && (mBitmap.getHeight() > mBitmap.getWidth())) {
                widthSize = heightSize * mBitmap.getWidth() / mBitmap.getHeight();
            } else if ((mBitmap.getWidth() > widthSize) && (mBitmap.getWidth() > mBitmap.getHeight())) {
                heightSize = widthSize * mBitmap.getHeight() / mBitmap.getWidth();
            } else {
                heightSize = mBitmap.getHeight();
                widthSize = mBitmap.getWidth();
            }
        }
        Log.d(TAG, "onMeasure: heightSize: " + heightSize + " widthSize: " + widthSize);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);
//        mOriginBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        add2HistoryBitmaps(mBitmap.copy(Bitmap.Config.ARGB_8888, true));
        Log.d(TAG, "onSizeChanged");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 根据图片尺寸缩放图片，同样只考虑了高大于宽的情况
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 如果你的界面有多个模式，你需要有个变量来判断当前是否可draw
        if (mDrawMode) {
            mPaint = mPenPaint;
        } else {
            mPaint = mEraserPaint;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                mPath.reset();
                mPath.moveTo(x, y);
                mX = x;
                mY = y;
                mCanvas.drawPath(mPath, mPaint);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
                mCanvas.drawPath(mPath, mPaint);
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(mX, mY);
                mCanvas.drawPath(mPath, mPaint);
                add2HistoryBitmaps(mBitmap.copy(Bitmap.Config.ARGB_8888, true));//添加到历史记录中
                isUndo = false;
                isRedo = false;
                mPath = null;
                break;
            default:
                break;
        }

        invalidate();
        return true;
    }

    public void initializePen() {
        mDrawMode = true;
        mPenPaint = null;
        mPenPaint = new Paint();
        mPenPaint.setAntiAlias(true);
        mPenPaint.setAlpha(mPenAlpha);
        mPenPaint.setDither(true);
        mPenPaint.setFilterBitmap(true);
        mPenPaint.setStyle(Paint.Style.STROKE);
        mPenPaint.setStrokeJoin(Paint.Join.ROUND);
        mPenPaint.setStrokeWidth(mPenSize);
        mPenPaint.setStrokeCap(Paint.Cap.ROUND);
        mPenPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
    }

    public void initializeEraser() {
        mDrawMode = false;
        Log.d(TAG, "initializeEraser");
        mEraserPaint = new Paint();
        mEraserPaint.setColor(Color.WHITE);
//        mEraserPaint.setAlpha(mEraserAlpla);
        //这个属性是设置paint为橡皮擦重中之重
        //这是重点
        //下面这句代码是橡皮擦设置的重点
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        //上面这句代码是橡皮擦设置的重点（重要的事是不是一定要说三遍）
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setStrokeWidth(mEraserSize);
    }

    /**
     * 对图片进行描边
     *
     * @param strokeColor 描边颜色
     * @param strokeSize  描边尺寸
     */
    public void strokeImage(@ColorInt int strokeColor, int strokeSize) {
        Paint p = new Paint();
        p.setColor(strokeColor);
        p.setStrokeWidth(strokeSize);
        p.setStyle(Paint.Style.STROKE);//空心矩形框
        if (mLoadBitmap != null) {
            mCanvas.drawRect((getWidth() - mLoadBitmap.getWidth()) / 2,
                    (getHeight() - mLoadBitmap.getHeight()) / 2,
                    (getWidth() - mLoadBitmap.getWidth()) / 2 + mLoadBitmap.getWidth(),
                    (getHeight() - mLoadBitmap.getHeight()) / 2 + mLoadBitmap.getHeight(),
                    p);
        }
        invalidate();
    }


    public void loadImage(Bitmap bitmap) {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        Matrix m = new Matrix();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        //判断载入图片的大小,进行等比例的缩放
        if (bitmapHeight > getHeight() || bitmapWidth > getWidth()) {
            Log.d(TAG, "Big Image");
            float heightScale = (float) getHeight() / bitmapHeight;
            float widthScale = (float) getWidth() / bitmapWidth;
            if (heightScale > widthScale) {
                m.postScale(widthScale, widthScale);
            } else {
                m.postScale(heightScale, heightScale);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        }
        mCanvas.drawBitmap(bitmap, (getWidth() - bitmap.getWidth()) / 2, (getHeight() - bitmap.getHeight()) / 2, null);
        mLoadBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        add2HistoryBitmaps(mBitmap.copy(Bitmap.Config.ARGB_8888, true));
        invalidate();
    }

    public void clear() {
        mCanvas.drawColor(backgroupColor, PorterDuff.Mode.CLEAR);
        setBackgroundColor(Color.WHITE);
        invalidate();
    }

    /**
     * 撤销
     */
    public void undo() {
        if (historyBitmaps != null && historyBitmaps.size() > 0) {
            // 清空画布
            mCanvas.drawColor(backgroupColor, PorterDuff.Mode.CLEAR);
            Bitmap undoBitmap = historyBitmapsRemoveLast();
            if (!isUndo) {
                isUndo = true;
                add2RemoveBitmaps(undoBitmap);
                undoBitmap = historyBitmapsRemoveLast();
            }
            mBitmap = undoBitmap.copy(Bitmap.Config.ARGB_8888, true);
            mCanvas = new Canvas(mBitmap);
            add2RemoveBitmaps(undoBitmap);
            invalidate();
        }
    }

    /**
     * 重做
     */
    public void redo() {
        if (removeBitmaps != null && removeBitmaps.size() > 0) {
            // 清空画布
            mCanvas.drawColor(backgroupColor, PorterDuff.Mode.CLEAR);
            Bitmap redoBitmap = removeBitmapsRemoveLast();
            if (!isRedo) {
                isRedo = true;
                add2HistoryBitmaps(redoBitmap);
                redoBitmap = removeBitmapsRemoveLast();
            }
            mBitmap = redoBitmap.copy(Bitmap.Config.ARGB_8888, true);
            mCanvas = new Canvas(mBitmap);
            add2HistoryBitmaps(redoBitmap);
            invalidate();
        }
    }

    /**
     * 保存图片
     *
     * @param filePath 路径名
     * @param filename 文件名
     * @param format   存储格式
     * @param quality  质量
     * @return 是否保存成功
     */
    public boolean saveImage(String filePath, String filename, Bitmap.CompressFormat format,
                             int quality) {
        if (quality > 100) {
            Log.d("saveImage", "quality cannot be greater that 100");
            return false;
        }
        File file;
        FileOutputStream out = null;
        try {
            switch (format) {
                case PNG:
                    file = new File(filePath, filename + ".png");
                    out = new FileOutputStream(file);
                    return mBitmap.compress(Bitmap.CompressFormat.PNG, quality, out);
                case JPEG:
                    file = new File(filePath, filename + ".jpg");
                    out = new FileOutputStream(file);
                    return mBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                default:
                    file = new File(filePath, filename + ".png");
                    out = new FileOutputStream(file);
                    return mBitmap.compress(Bitmap.CompressFormat.PNG, quality, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    private void add2HistoryBitmaps(Bitmap bitmap) {
        //避免数组过大造成内存占用太多
        if (historyBitmaps.size() > 15) {
            Bitmap tmp = historyBitmaps.removeFirst();
            tmp.recycle();
            System.gc();
        }
        historyBitmaps.add(bitmap);
        Log.d(TAG, "historyBitmaps:" + historyBitmaps.size());

    }

    private Bitmap historyBitmapsRemoveLast() {
        return historyBitmaps.removeLast();
    }

    private void add2RemoveBitmaps(Bitmap bitmap) {
        removeBitmaps.add(bitmap);
    }

    private Bitmap removeBitmapsRemoveLast() {
        return removeBitmaps.removeLast();
    }


    /**
     * 设置背景色
     *
     * @param color
     */
    @Override
    public void setBackgroundColor(int color) {
        backgroupColor = color;
        int[] fillColor = new int[getWidth() * getHeight()];
        Arrays.fill(fillColor, backgroupColor);
        mCanvas.drawColor(color);
        super.setBackgroundColor(color);
        add2HistoryBitmaps(mBitmap.copy(Bitmap.Config.ARGB_8888, true));
    }

    public void setPenSize(float size) {
        mPenSize = size;
        mPenPaint.setStrokeWidth(size);
    }

    public float getPenSize() {
        return mPenPaint.getStrokeWidth();
    }

    public void setPenColor(@ColorInt int color) {
        mPenPaint.setColor(color);
    }

    public
    @ColorInt
    int getPenColor() {
        return mPenPaint.getColor();
    }

    public void setEraserSize(float mEraserSize) {
        this.mEraserSize = mEraserSize;
        mEraserPaint.setStrokeWidth(mEraserSize);
    }

    public float getEraserSize(){
        return mEraserSize;
    }
    /**
     * @return 当前画布上的内容
     */
    public Bitmap getImageBitmap() {
        return mBitmap;
    }

    public void setPenAlpha(int mPenAlpha) {
        this.mPenAlpha = mPenAlpha;
        mPenPaint.setAlpha(mPenAlpha);
    }
    public int getPenAlpha(){
        return mPenAlpha;
    }
}