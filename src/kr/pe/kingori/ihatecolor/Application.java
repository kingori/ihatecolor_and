package kr.pe.kingori.ihatecolor;

import android.content.Context;
import kr.pe.kingori.ihatecolor.util.FontManager;

public class Application extends android.app.Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        FontManager.init(this);
        context = this;
    }
}
