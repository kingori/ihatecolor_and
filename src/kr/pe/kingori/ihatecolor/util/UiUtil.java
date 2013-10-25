package kr.pe.kingori.ihatecolor.util;

import android.widget.Toast;
import kr.pe.kingori.ihatecolor.Application;

public class UiUtil {
    private UiUtil() {
    }

    public static void showToast(String message) {
        Toast.makeText(Application.context, message, Toast.LENGTH_SHORT).show();
    }
}
