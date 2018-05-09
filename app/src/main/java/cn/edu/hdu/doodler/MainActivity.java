package cn.edu.hdu.doodler;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.edu.hdu.doodler.view.ColorPickerPopupWindow;
import cn.edu.hdu.doodler.view.DoodleView;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,View.OnTouchListener {
    private ImageView mSaveButton, mPenButton, mPenColorButton, mBackgroundColorButton, mLoadButton, mUndoButton,mRedoButton, mShareButton;
    private DoodleView mDrawingView;
    private SeekBar mPenSizeSeekbar, mEraserSeekbar;
    private View mActionBarView;
    private int mPenColor=Color.BLACK;
    public final static int IMAGE_REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setCustomActionBar();
        initializeUI();
        setListeners();

    }

    private void setCustomActionBar() {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout, null);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(mActionBarView, lp);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

        }
    }

    private void setListeners() {
        mSaveButton.setOnClickListener(this);
        mPenButton.setOnClickListener(this);
//        mEraserButton.setOnClickListener(this);
        mPenColorButton.setOnClickListener(this);
        mBackgroundColorButton.setOnClickListener(this);
        mPenSizeSeekbar.setOnSeekBarChangeListener(this);
//        mEraserSeekbar.setOnSeekBarChangeListener(this);
        mLoadButton.setOnClickListener(this);
        mRedoButton.setOnClickListener(this);
        mUndoButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);

        mSaveButton.setOnTouchListener(this);
        mLoadButton.setOnTouchListener(this);
        mPenButton.setOnTouchListener(this);
        mPenColorButton.setOnTouchListener(this);
        mBackgroundColorButton.setOnTouchListener(this);
        mRedoButton.setOnTouchListener(this);
        mUndoButton.setOnTouchListener(this);
        mShareButton.setOnTouchListener(this);
    }

    private void initializeUI() {
        mDrawingView            = findViewById(R.id.scratch_pad);
        mSaveButton             = mActionBarView.findViewById(R.id.save_bar_btn);
        mLoadButton             = mActionBarView.findViewById(R.id.photo_bar_btn);
        mPenButton              = mActionBarView.findViewById(R.id.pen_bar_btn);
//        mEraserButton           = mActionBarView.findViewById(R.id.eraser_bar_btn);
        mPenColorButton         = mActionBarView.findViewById(R.id.color_bar_btn);
        mBackgroundColorButton  = mActionBarView.findViewById(R.id.background_color_bar_btn);
        mRedoButton             = mActionBarView.findViewById(R.id.redo_bar_btn);
        mUndoButton             = mActionBarView.findViewById(R.id.undo_bar_btn);
        mShareButton             = mActionBarView.findViewById(R.id.share_bar_btn);
        mPenSizeSeekbar         = findViewById(R.id.pen_size_seekbar);
//        mEraserSeekbar          = findViewById(R.id.eraser_size_seekbar);

        //初始化界面时 默认选中画笔
        onClick(mPenButton);

    }

    @Override
    public void onClick(View view) {
        mDrawingView.setBackground(null);
        mSaveButton.setBackground(null);
        mLoadButton.setBackground(null);
        mPenButton.setBackground(null);
//        mEraserButton.setBackground(null);
        mPenColorButton.setBackground(null);
        mBackgroundColorButton.setBackground(null);
        switch (view.getId()) {
            case R.id.save_bar_btn:
                String fileName=String.valueOf(System.currentTimeMillis());
                mDrawingView.saveImage(Environment.getExternalStorageDirectory().toString(), fileName,
                        Bitmap.CompressFormat.PNG, 100);
                Toast.makeText(getApplicationContext(),"保存的路径为：\n"+Environment.getExternalStorageDirectory().toString()+ fileName+"png",Toast.LENGTH_SHORT).show();
                break;
            case R.id.photo_bar_btn:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
                break;
            case R.id.undo_bar_btn:
                mDrawingView.undo();
                onClick(mPenButton);
                break;
            case R.id.redo_bar_btn:
                mDrawingView.redo();
                onClick(mPenButton);
                break;

            case R.id.pen_bar_btn:
//                mDrawingView.initializePen();
                mDrawingView.setPenColor(mPenColor);
                mPenButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
            case R.id.color_bar_btn:
                final ColorPickerPopupWindow colorPickerPopupWindow=new ColorPickerPopupWindow(mPenColorButton,getApplicationContext());
                //监听窗口关闭回调事件 当取色器关闭时，选择为画笔
                colorPickerPopupWindow.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mPenColor=colorPickerPopupWindow.selectColor;;
                        //触发画笔点击事件
                        onClick(mPenButton);
                    }
                });
                mPenColorButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
            case R.id.background_color_bar_btn:
                final ColorPickerPopupWindow backgroundColorPickerPopupWindow=new ColorPickerPopupWindow(mPenColorButton,getApplicationContext());
                //监听窗口关闭回调事件 当取色器关闭时，选择为画笔
                backgroundColorPickerPopupWindow.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mDrawingView.setBackgroundColor(backgroundColorPickerPopupWindow.selectColor);
                    }
                });
                mBackgroundColorButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;

            case R.id.share_bar_btn:
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
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.pen_size_seekbar:
                mDrawingView.setPenSize(i);
                break;
//            case R.id.eraser_size_seekbar:
//                mDrawingView.setEraserSize(i);
//                break;
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
     * @param bitmap
     * @return
     */
    public File bitMap2File(Bitmap bitmap) {


        String path = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = Environment.getExternalStorageDirectory() + File.separator;//保存到sd根目录下
        }


        //        File f = new File(path, System.currentTimeMillis() + ".jpg");
        File f = new File(path, String.valueOf(System.currentTimeMillis() )+ ".png");
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
        switch (event.getAction()){
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