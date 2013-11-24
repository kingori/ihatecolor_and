package kr.pe.kingori.ihatecolor.util;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;
import android.widget.Toast;
import kr.pe.kingori.ihatecolor.Application;

public class UiUtil {
    private UiUtil() {
    }

    public static void showToast(String message) {
        Toast.makeText(Application.context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isWideDevice(Context context) {
        Point size = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getSize(size);
        return (float) size.x / size.y > 0.72;
    }
}
