package kr.pe.kingori.ihatecolor.util;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

public class FontManager {
    private static Typeface customTypeFace;

    private FontManager() {
    }

    public static void init(Context context) {
        customTypeFace = Typeface.createFromAsset(context.getAssets(), "fonts/intro.otf");
    }

    public static TextView applyFont(TextView view) {
        view.setTypeface(customTypeFace);
        return view;
    }
}
