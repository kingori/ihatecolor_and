package kr.pe.kingori.ihatecolor.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import kr.pe.kingori.ihatecolor.R;


public class SplashActivity extends Activity {
    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spalsh);
        h = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1500);
    }
}
