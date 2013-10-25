package kr.pe.kingori.ihatecolor.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import kr.pe.kingori.ihatecolor.util.FontManager;

public class CustomFontButton extends Button {
    public CustomFontButton(Context context) {
        super(context);
        applyTypeFace();
    }

    public CustomFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTypeFace();
    }

    public CustomFontButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyTypeFace();
    }

    private void applyTypeFace() {
        FontManager.applyFont(this);
    }
}
