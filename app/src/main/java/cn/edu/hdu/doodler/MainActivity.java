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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.edu.hdu.doodler.view.DoodleView;
import wang.relish.colorpicker.ColorPickerDialog;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, NavigationView.OnNavigationItemSelectedListener {

    public final static int IMAGE_REQUEST_CODE = 1;


    private ImageButton mPenButton, mUndoButton, mRedoButton, mEraserButton, mToolsButton;
    private DoodleView mDrawingView;
    private SeekBar mPenSizeSeekbar,mPenAlphaSeekbar, mEraserSeekbar;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, null, 0, 0);
        // 添加此句，toolbar左上角显示开启侧边栏图标
        mDrawerToggle.syncState();
        drawerLayout.addDrawerListener(mDrawerToggle);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
//         禁用手势
//        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//        // 打开
//        drawerLayout.openDrawer(Gravity.LEFT);
//        // 关闭
//        drawerLayout.closeDrawer(Gravity.LEFT);
    }


    private void initializeUI() {

        //获取控件
        mDrawingView = (DoodleView) findViewById(R.id.scratch_pad);

        mPenButton = (ImageButton) findViewById(R.id.pen_btn);
        mUndoButton = (ImageButton) findViewById(R.id.undo_btn);
        mRedoButton = (ImageButton) findViewById(R.id.redo_btn);
        mEraserButton = (ImageButton) findViewById(R.id.eraser_btn);
        mToolsButton = (ImageButton) findViewById(R.id.tools_btn);


        //设置监听器
        mPenButton.setOnClickListener(this);
        mUndoButton.setOnClickListener(this);
        mRedoButton.setOnClickListener(this);
        mEraserButton.setOnClickListener(this);
        mToolsButton.setOnClickListener(this);

        //默认画笔
        mDrawingView.initializePen();

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.undo_btn:
                mDrawingView.undo();
                break;
            case R.id.redo_btn:
                mDrawingView.redo();
                break;

            case R.id.pen_btn:
                mDrawingView.initializePen();
                PopupWindow penPopupWindow=new PopupWindow(getApplicationContext());
                View penAttrView=View.inflate(getApplicationContext(),R.layout.pen_param_layout,null);
                penPopupWindow.setContentView(penAttrView);
                penPopupWindow.setWidth(mDrawingView.getWidth());
                penPopupWindow.setHeight(200);
                mPenSizeSeekbar=penAttrView.findViewById(R.id.pen_size_seekBar);
                mPenSizeSeekbar.setOnSeekBarChangeListener(this);
                mPenAlphaSeekbar=penAttrView.findViewById(R.id.pen_alpha_seekBar);
                mPenAlphaSeekbar.setOnSeekBarChangeListener(this);
                mPenSizeSeekbar.setProgress((int)mDrawingView.getPenSize());
                mPenAlphaSeekbar.setProgress(mDrawingView.getPenAlpha());
                penPopupWindow.update();
                penPopupWindow.setFocusable(true);
                penPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
                penPopupWindow.showAsDropDown(mPenButton,0,2,Gravity.CENTER);
                break;

            case R.id.eraser_btn:
                mDrawingView.initializeEraser();
                PopupWindow eraserPopupWindow=new PopupWindow(getApplicationContext());
                View eraserAttrView=View.inflate(getApplicationContext(),R.layout.eraser_param_layout,null);
                eraserPopupWindow.setContentView(eraserAttrView);
                mEraserSeekbar=eraserAttrView.findViewById(R.id.eraser_size_seekBar);
                mEraserSeekbar.setOnSeekBarChangeListener(this);
                mEraserSeekbar.setProgress((int) mDrawingView.getEraserSize());
                eraserPopupWindow.setWidth(mDrawingView.getWidth());
                eraserPopupWindow.setHeight(200);
                eraserPopupWindow.setFocusable(true);
                eraserPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
                eraserPopupWindow.showAsDropDown(mEraserButton,0,2,Gravity.CENTER);
                break;

            case R.id.tools_btn:
                drawerLayout.openDrawer(Gravity.LEFT);
                break;

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.pen_size_seekBar:
                mDrawingView.setPenSize(i);
                break;
            case R.id.pen_alpha_seekBar:
                mDrawingView.setPenAlpha(i);
                break;
            case R.id.eraser_size_seekBar:
                mDrawingView.setEraserSize(i);
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


        //        File f = new File(path, System.currentTimeMillis() + ".jpg");
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                String fileName = String.valueOf(System.currentTimeMillis());
                mDrawingView.saveImage(Environment.getExternalStorageDirectory().toString(), fileName,
                        Bitmap.CompressFormat.PNG, 100);
                Toast.makeText(getApplicationContext(), "保存的路径为：\n" + Environment.getExternalStorageDirectory().toString() + fileName + ".png", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;

            case R.id.share_item:
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
                drawerLayout.closeDrawers();
                break;

            case R.id.wallet_item:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;


            case R.id.pen_color_item:
                ColorPickerDialog ss = new ColorPickerDialog.Builder(MainActivity.this, mDrawingView.getPenColor())   //mColor:初始颜色
                        .setHexValueEnabled(true)               //是否显示颜色值
                        .setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int i) {
//                                Toast.makeText(getApplicationContext(), "" + i, Toast.LENGTH_SHORT).show();
                                mDrawingView.setPenColor(i);
                            }
                        }) //设置监听颜色改变的监听器
                        .build();
//                ss.setOnDismissListener();
                ss.show();//展示
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.background_color_item:
                new ColorPickerDialog.Builder(this, mDrawingView.getBackgroundColor())   //mColor:初始颜色
                        .setHexValueEnabled(true)               //是否显示颜色值
                        .setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int i) {
                                mDrawingView.setBackgroundColor(i);
                            }
                        }) //设置监听颜色改变的监听器
                        .build()
                        .show();//展示
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.clear_item:
                mDrawingView.clear();
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
        }

        return true;
    }
}