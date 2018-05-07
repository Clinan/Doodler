package cn.edu.hdu.doodler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import com.christophesmet.android.views.colorpicker.ColorPickerView;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import cn.edu.hdu.doodler.view.DoodleView;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private ImageView mSaveButton, mPenButton, mEraserButton, mPenColorButton, mBackgroundColorButton,
            mLoadButton;
    private DoodleView mDrawingView;
    private SeekBar mPenSizeSeekbar, mEraserSeekbar;
    private View mActionBarView;
    private int mPenColor=Color.BLACK;

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
        mEraserButton.setOnClickListener(this);
        mPenColorButton.setOnClickListener(this);
        mBackgroundColorButton.setOnClickListener(this);
        mPenSizeSeekbar.setOnSeekBarChangeListener(this);
        mEraserSeekbar.setOnSeekBarChangeListener(this);
        mLoadButton.setOnClickListener(this);
    }

    private void initializeUI() {
        mDrawingView            = findViewById(R.id.scratch_pad);
        mSaveButton             = mActionBarView.findViewById(R.id.save_bar_btn);
        mLoadButton             = mActionBarView.findViewById(R.id.photo_bar_btn);
        mPenButton              = mActionBarView.findViewById(R.id.pen_bar_btn);
        mEraserButton           = mActionBarView.findViewById(R.id.eraser_bar_btn);
        mPenColorButton         = mActionBarView.findViewById(R.id.color_bar_btn);
        mBackgroundColorButton  = mActionBarView.findViewById(R.id.background_color_bar_btn);
        mPenSizeSeekbar         = findViewById(R.id.pen_size_seekbar);
        mEraserSeekbar          = findViewById(R.id.eraser_size_seekbar);
    }

    @Override
    public void onClick(View view) {
        mDrawingView.setBackground(null);
        mSaveButton.setBackground(null);
        mLoadButton.setBackground(null);
        mPenButton.setBackground(null);
        mEraserButton.setBackground(null);
        mPenColorButton.setBackground(null);
        mBackgroundColorButton.setBackground(null);
        switch (view.getId()) {
            case R.id.save_bar_btn:
                mDrawingView.saveImage(Environment.getExternalStorageDirectory().toString(), "test",
                        Bitmap.CompressFormat.PNG, 100);
                mSaveButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
            case R.id.photo_bar_btn:
                mDrawingView.loadImage(BitmapFactory.decodeResource(getResources(), R.drawable.king));
                Log.d("saveImage", "quality cannot better that 100");
                mLoadButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
            case R.id.pen_bar_btn:
                mDrawingView.initializePen();
                mPenButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
            case R.id.eraser_bar_btn:
//                mDrawingView.initializeEraser();
                mEraserButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
            case R.id.color_bar_btn:
                int selectColor=selectColorDialog();
                mDrawingView.setPenColor(selectColor);
                mPenColorButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
            case R.id.background_color_bar_btn:
                final ColorPicker backgroundColorPicker = new ColorPicker(MainActivity.this, 100, 100, 100);
                backgroundColorPicker.setCallback(
                        new ColorPickerCallback() {
                            @Override
                            public void onColorChosen(int color) {
                                mDrawingView.setBackgroundColor(color);
                                backgroundColorPicker.dismiss();
                            }
                        });
                backgroundColorPicker.show();
                mBackgroundColorButton.setBackgroundColor(getResources().getColor(R.color.colorBarBtn));
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.pen_size_seekbar:
                mDrawingView.setPenSize(i);
                break;
            case R.id.eraser_size_seekbar:
//                mDrawingView.setEraserSize(i);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private  int selectColorDialog(){
        final int[] selectColor = new int[1];
        PopupWindow popupWindow=new PopupWindow(getApplicationContext());
        View pwview = View.inflate(getApplicationContext(), R.layout.color_select, null);
        popupWindow.setContentView(pwview);
        final EditText colorTextEt=pwview.findViewById(R.id.color_text_et);
        popupWindow.setWidth(720);
        popupWindow.setHeight(1080);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F8F8F8")));
        // 设置可以获取焦点
        popupWindow.setFocusable(true);
        popupWindow.update();
        popupWindow.showAsDropDown(mEraserButton, 0, 50);
        ColorPickerView mColorPickerView=pwview.findViewById(R.id.colorpicker);
        final View mColorView=pwview.findViewById(R.id.result_color);
        mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(final int color) {
                mColorView.setBackgroundColor(color);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        colorTextEt.setText("#"+Integer.toHexString(color));

                    }
                });
                selectColor[0] =color;
            }
        });
        return selectColor[0];
    }
}