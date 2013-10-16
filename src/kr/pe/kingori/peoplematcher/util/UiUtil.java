package kr.pe.kingori.peoplematcher.util;

import android.widget.Toast;
import kr.pe.kingori.peoplematcher.Application;

public class UiUtil {
    private UiUtil() {
    }

    public static void showToast(String message) {
        Toast.makeText(Application.context, message, Toast.LENGTH_SHORT).show();
    }
}
