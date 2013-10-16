package kr.pe.kingori.peoplematcher;

import android.content.Context;

public class Application extends android.app.Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
