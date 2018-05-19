package cn.edu.hdu.doodler.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.christophesmet.android.views.colorpicker.ColorPickerView;

import cn.edu.hdu.doodler.R;

public class ColorPickerPopupWindow {
    public int selectColor;
    public PopupWindow popupWindow;
    public ColorPickerPopupWindow(View view,Context context){
         popupWindow=new PopupWindow(context);
        View pwview = View.inflate(context, R.layout.color_select, null);
        popupWindow.setContentView(pwview);
        popupWindow.setWidth(720);
        popupWindow.setHeight(1080);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F8F8F8")));
        // 设置可以获取焦点
        popupWindow.setFocusable(true);
        popupWindow.update();
        popupWindow.showAsDropDown(view, 0, 50);
        final EditText colorTextEt=pwview.findViewById(R.id.color_text_et);
        ColorPickerView mColorPickerView=pwview.findViewById(R.id.colorpicker);
        final View mColorView=pwview.findViewById(R.id.result_color);
        mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(final int color) {
                mColorView.setBackgroundColor(color);
                colorTextEt.setText("#"+Integer.toHexString(color).substring(2));
                selectColor =color;
            }

        });

    }

}
