package cn.edu.hdu.doodler;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import com.christophesmet.android.views.colorpicker.ColorPickerView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.edu.hdu.doodler.view.ColorPickerPopupWindow;
import cn.edu.hdu.doodler.view.DoodleView;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {
    private static final String TAG = "MainActivity";
    private ImageView mSaveButton, mPenButton, mPenColorButton, mBackgroundColorButton, mLoadButton, mUndoButton, mRedoButton, mShareButton, mClearButton, mEraserButton, mStrokeButton;
    //    private ImageButton mEraserButton;
    private DoodleView mDrawingView;
    private SeekBar mPenSizeSeekbar,mPenAlphaSeekbar;
    private int mPenColor = Color.BLACK;
    public final static int IMAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        setListeners();

    }


    private void setListeners() {
        mPenSizeSeekbar.setOnSeekBarChangeListener(this);
        mPenAlphaSeekbar.setOnSeekBarChangeListener(this);


        mSaveButton.setOnClickListener(this);
        mPenButton.setOnClickListener(this);
        mPenColorButton.setOnClickListener(this);
        mBackgroundColorButton.setOnClickListener(this);
        mLoadButton.setOnClickListener(this);
        mRedoButton.setOnClickListener(this);
        mUndoButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
        mEraserButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);
        mStrokeButton.setOnClickListener(this);


        mSaveButton.setOnTouchListener(this);
        mLoadButton.setOnTouchListener(this);
        mPenButton.setOnTouchListener(this);
        mPenColorButton.setOnTouchListener(this);
        mBackgroundColorButton.setOnTouchListener(this);
        mRedoButton.setOnTouchListener(this);
        mUndoButton.setOnTouchListener(this);
        mShareButton.setOnTouchListener(this);
        mEraserButton.setOnTouchListener(this);
        mClearButton.setOnTouchListener(this);
        mStrokeButton.setOnTouchListener(this);
    }

    private void initializeUI() {
        mDrawingView = findViewById(R.id.scratch_pad);
        mSaveButton = findViewById(R.id.save_btn);
        mLoadButton = findViewById(R.id.photo_btn);
        mPenButton = findViewById(R.id.pen_btn);
        mPenColorButton = findViewById(R.id.color_btn);
        mBackgroundColorButton = findViewById(R.id.background_color_btn);
        mRedoButton = findViewById(R.id.redo_btn);
        mUndoButton = findViewById(R.id.undo_btn);
        mShareButton = findViewById(R.id.share_btn);
        mEraserButton = findViewById(R.id.eraser_btn);
        mClearButton = findViewById(R.id.clear_btn);
        mStrokeButton = findViewById(R.id.stroke_btn);


        mPenAlphaSeekbar = findViewById(R.id.pen_alpha_seekbar);
        mPenSizeSeekbar = findViewById(R.id.pen_size_seekbar);
        //初始化界面时 默认选中画笔
//        onClick(mPenButton);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mPenAlphaSeekbar.setProgress(mDrawingView.getPenAlpha());
                mPenSizeSeekbar.setProgress((int) mDrawingView.getPenSize());
                Log.d("PenSize",mDrawingView.getPenSize()+"");
            }
        });
    }

    @Override
    public void onClick(View view) {
        mDrawingView.setBackground(null);
        mSaveButton.setBackground(null);
        mLoadButton.setBackground(null);
        mPenButton.setBackground(null);
        mPenColorButton.setBackground(null);
        mBackgroundColorButton.setBackground(null);
        switch (view.getId()) {
            case R.id.save_btn:
                String fileName = String.valueOf(System.currentTimeMillis());
                mDrawingView.saveImage(Environment.getExternalStorageDirectory().toString(), fileName,
                        Bitmap.CompressFormat.PNG, 100);
                Toast.makeText(getApplicationContext(), "保存的路径为：\n" + Environment.getExternalStorageDirectory().toString() + fileName + ".png", Toast.LENGTH_SHORT).show();
                break;
            case R.id.photo_btn:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
                break;
            case R.id.undo_btn:
                mDrawingView.undo();
                onClick(mPenButton);
                break;
            case R.id.redo_btn:
                mDrawingView.redo();
                onClick(mPenButton);
                break;


            case R.id.pen_btn:
                mDrawingView.initializePen();
                mDrawingView.setPenColor(mPenColor);
                mPenButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
            case R.id.color_btn:
                final ColorPickerPopupWindow colorPickerPopupWindow = new ColorPickerPopupWindow(mPenColorButton, getApplicationContext());
                //监听窗口关闭回调事件 当取色器关闭时，选择为画笔
                colorPickerPopupWindow.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mPenColor = colorPickerPopupWindow.selectColor;
                        ;
                        mDrawingView.setPenColor(mPenColor);
                    }
                });
                mPenColorButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
            case R.id.background_color_btn:
                final ColorPickerPopupWindow backgroundColorPickerPopupWindow = new ColorPickerPopupWindow(mPenColorButton, getApplicationContext());
                //监听窗口关闭回调事件 当取色器关闭时，选择为画笔
                backgroundColorPickerPopupWindow.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mDrawingView.setBackgroundColor(backgroundColorPickerPopupWindow.selectColor);
                    }
                });
                mBackgroundColorButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;

            case R.id.share_btn:
                File file = bitMap2File(compressImage(getBitmapByView(mDrawingView)));
                if (file != null && file.exists() && file.isFile()) {
                    //由文件得到uri
                    Uri imageUri = Uri.fromFile(file);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "分享图片"));
                }
                break;
            case R.id.eraser_btn:
                mDrawingView.initializeEraser();
                PopupWindow eraser_pw = new PopupWindow(getApplicationContext());
                View eraserView = View.inflate(getApplicationContext(), R.layout.eraser_layout, null);
                SeekBar eraserSizeSeekbar = eraserView.findViewById(R.id.eraser_size_seekbar);
                eraserSizeSeekbar.setProgress((int) mDrawingView.getEraserSize());
                eraserSizeSeekbar.setOnSeekBarChangeListener(this);
                eraser_pw.setContentView(eraserView);
                eraser_pw.setWidth(720);
                eraser_pw.setHeight(mEraserButton.getHeight());
                eraser_pw.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CCCCCC")));
                // 设置可以获取焦点
                eraser_pw.setFocusable(true);
                eraser_pw.update();
                eraser_pw.showAsDropDown(view, mEraserButton.getWidth() + 10, -mEraserButton.getHeight(), Gravity.CENTER);
                break;
            case R.id.clear_btn:
                mDrawingView.clear();
                break;

            case R.id.stroke_btn:
                PopupWindow popupWindow = new PopupWindow(getApplicationContext());
                View pwview = View.inflate(getApplicationContext(), R.layout.color_select, null);
                final SeekBar strokeSizeSeekBar = pwview.findViewById(R.id.stroke_size_seekbar);
                pwview.findViewById(R.id.bar_layout).setVisibility(View.VISIBLE);
                popupWindow.update();
//                strokeSizeSeekBar.setOnSeekBarChangeListener(this);
                ColorPickerView mColorPickerView = pwview.findViewById(R.id.colorpicker);
                final View mColorView = pwview.findViewById(R.id.result_color);
                final EditText colorTextEt = pwview.findViewById(R.id.color_text_et);
                final int[] selectColor = new int[1];
                mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {

                    @Override
                    public void onColorSelected(final int color) {
                        mColorView.setBackgroundColor(color);
                        colorTextEt.setText("#" + Integer.toHexString(color).substring(2));
                        selectColor[0] = color;
                    }

                });
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mDrawingView.strokeImage(selectColor[0], strokeSizeSeekBar.getProgress());
                    }
                });
                popupWindow.setContentView(pwview);
                popupWindow.setWidth(720);
                popupWindow.setHeight(1200);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F8F8F8")));
                // 设置可以获取焦点
                popupWindow.setFocusable(true);
                popupWindow.update();
                popupWindow.showAsDropDown(view, 0, 50);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.pen_size_seekbar:
                mDrawingView.setPenSize(i);
                Log.d("PenSize",mDrawingView.getPenSize()+"");

                break;
            case R.id.eraser_size_seekbar:
                mDrawingView.setEraserSize(i);
                break;
            case R.id.pen_alpha_seekbar:
                mDrawingView.setPenAlpha(i);
                Log.d("pen_alpha_seekbar",""+i);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        //在相册里面选择好相片之后调回到现在的这个activity中
        switch (requestCode) {
            case IMAGE_REQUEST_CODE://这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
                if (resultCode == RESULT_OK) {//resultcode是setResult里面设置的code值
                    try {
                        Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String path = cursor.getString(columnIndex); //获取照片路径
                        cursor.close();
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        mDrawingView.loadImage(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    /**
     * 将布局转化为bitmap 这里传入的是要截的布局的根View
     *
     * @param headerView
     * @return
     */
    public Bitmap getBitmapByView(View headerView) {
        int h = headerView.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(headerView.getWidth(), h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        headerView.draw(canvas);
        return bitmap;
    }

    /**
     * 把截图获取的bitmap做简单的压缩
     *
     * @param image
     * @return
     */
    private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }


    /**
     * 把压缩过的图片先保存到本地才能调用系统分享出去，因为系统分享的是一个uri,我们需要先把bitmap转为本地file文件 把bitmap转化为file
     *
     * @param bitmap
     * @return
     */
    public File bitMap2File(Bitmap bitmap) {
        String path = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = Environment.getExternalStorageDirectory() + File.separator;//保存到sd根目录下
        }
        File f = new File(path, String.valueOf(System.currentTimeMillis()) + ".png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            bitmap.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return f;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                onClick(v);
                break;
            case MotionEvent.ACTION_UP:
                v.setBackground(null);
                break;
        }
        return true;
    }
}