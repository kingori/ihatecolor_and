package kr.pe.kingori.ihatecolor.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import kr.pe.kingori.ihatecolor.util.FontManager;

public class CustomFontTextView extends TextView {
    public CustomFontTextView(Context context) {
        super(context);
        applyTypeFace();
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTypeFace();
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyTypeFace();
    }

    private void applyTypeFace() {
        FontManager.applyFont(this);
    }
}
