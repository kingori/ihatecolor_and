package kr.pe.kingori.ihatecolor.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import kr.pe.kingori.ihatecolor.debug.R;
import kr.pe.kingori.ihatecolor.ui.event.DialogEvent;

public class CustomDialogFragment
        extends DialogFragment implements View.OnClickListener {

    private static final String BDL_SHOW_DECLINE = "show_decline";
    private static final String BDL_MSG = "msg";
    private static final String BDL_OK_TEXT = "ok_text";
    private static final String BDL_DECLINE_TEXT = "decline_text";
    private static final String BDL_TYPE = "type";

    public static CustomDialogFragment newInstance(DialogEvent.DialogType dialogType, boolean showDecline, String msg, String okText, String declineText) {
        CustomDialogFragment f = new CustomDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(BDL_TYPE, dialogType);
        args.putBoolean(BDL_SHOW_DECLINE, showDecline);
        args.putString(BDL_MSG, msg);
        args.putString(BDL_OK_TEXT, okText);
        args.putString(BDL_DECLINE_TEXT, declineText);
        f.setArguments(args);

        return f;
    }

    public CustomDialogFragment() {
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.diag_custom);
        Button btDecline = (Button) dialog.findViewById(R.id.bt_decline);
        Button btAccept = (Button) dialog.findViewById(R.id.bt_accept);

        if (getArguments().getBoolean(BDL_SHOW_DECLINE)) {
            btDecline.setVisibility(View.VISIBLE);
            btDecline.setText(getArguments().getString(BDL_DECLINE_TEXT));
        } else {
            btDecline.setVisibility(View.GONE);
        }

        ((TextView) dialog.findViewById(R.id.tv_msg)).setText(getArguments().getString(BDL_MSG));

        btDecline.setOnClickListener(this);
        btAccept.setOnClickListener(this);
        btAccept.setText(getArguments().getString(BDL_OK_TEXT));
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_decline:
                EventBus.getDefault().post(new DialogEvent((DialogEvent.DialogType) getArguments().getSerializable(BDL_TYPE), DialogEvent.ButtonType.CANCEL));
                dismiss();
            case R.id.bt_accept:
                EventBus.getDefault().post(new DialogEvent((DialogEvent.DialogType) getArguments().getSerializable(BDL_TYPE), DialogEvent.ButtonType.OK));
                dismiss();
        }
    }

}
