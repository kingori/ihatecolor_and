package kr.pe.kingori.ihatecolor.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import kr.pe.kingori.ihatecolor.debug.R;

public class CustomDialog extends Dialog implements View.OnClickListener {

    private OnClickListener listener = null;

    public CustomDialog(Context context, boolean showDecline, OnClickListener listener, String msg, String okText, String declineText) {
        super(context, false, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.diag_custom);

        this.listener = listener;

        Button btDecline = (Button) findViewById(R.id.bt_decline);
        Button btAccept = (Button) findViewById(R.id.bt_accept);

        if (showDecline) {
            btDecline.setVisibility(View.VISIBLE);
            btDecline.setText(declineText);
        } else {
            btDecline.setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.tv_msg)).setText(msg);

        btDecline.setOnClickListener(this);
        btAccept.setOnClickListener(this);
        btAccept.setText(okText);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            switch (v.getId()) {
                case R.id.bt_decline:
                    listener.onDecline();
                    dismiss();
                case R.id.bt_accept:
                    listener.onAccept();
                    dismiss();
            }
        }
    }


    public static interface OnClickListener {
        void onDecline();

        void onAccept();
    }
}
