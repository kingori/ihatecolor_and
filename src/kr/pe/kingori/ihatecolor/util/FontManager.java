package kr.pe.kingori.ihatecolor.util;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.widget.TextView;
import kr.pe.kingori.ihatecolor.R;

public class FontManager {
    private static Typeface customTypeFace;

    private FontManager() {
    }

    public static void init(Context context) {
        customTypeFace = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.typeface_path));
    }

    public static TextView applyTypeface(TextView view) {
        view.setTypeface(customTypeFace, Typeface.BOLD);
        return view;
    }

    public static TextPaint applyTypeface(TextPaint tp) {
        tp.setTypeface(customTypeFace);
        return tp;
    }
}
