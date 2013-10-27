package kr.pe.kingori.ihatecolor.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import kr.pe.kingori.ihatecolor.debug.R;
import kr.pe.kingori.ihatecolor.model.GameMode;
import kr.pe.kingori.ihatecolor.ui.event.PlayEvent;
import kr.pe.kingori.ihatecolor.util.UiUtil;

import static android.widget.RelativeLayout.LayoutParams;

public class MainFragment extends BaseFragment implements View.OnClickListener {
    private View view;
    private View vgLogo;
    private View vgGameBtns;
    private View btSignout;
    private View btSignIn;
    private View vgSingleSub;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_main, container, false);
        vgLogo = view.findViewById(R.id.vg_logo);
        vgGameBtns = view.findViewById(R.id.vg_game_btns);
        btSignIn = view.findViewById(R.id.bt_signin);
        btSignout = view.findViewById(R.id.bt_sign_out);
        vgSingleSub = view.findViewById(R.id.vg_single_sub);

        view.findViewById(R.id.bt_signin).setOnClickListener(this);
        btSignout.setOnClickListener(this);
        view.findViewById(R.id.bt_single).setOnClickListener(this);
        view.findViewById(R.id.bt_single_4).setOnClickListener(this);
        view.findViewById(R.id.bt_single_6).setOnClickListener(this);
        view.findViewById(R.id.bt_quick).setOnClickListener(this);
        view.findViewById(R.id.bt_multi).setOnClickListener(this);
        view.findViewById(R.id.bt_achievement).setOnClickListener(this);
        view.findViewById(R.id.bt_leaderboard).setOnClickListener(this);
        view.findViewById(R.id.bt_about_us).setOnClickListener(this);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        showButtons();
    }

    private void showButtons() {
        boolean loggedIn = getBaseActivity().isUserSignedIn();
        if (loggedIn) {
            {
                LayoutParams lParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                lParams.topMargin = getResources().getDimensionPixelSize(R.dimen.main_logo_top_margin);
                vgLogo.setLayoutParams(lParams);
            }

            vgGameBtns.setVisibility(View.VISIBLE);

            btSignout.setVisibility(View.VISIBLE);
            btSignIn.setVisibility(View.GONE);
        } else {
            {
                LayoutParams lParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                vgLogo.setLayoutParams(lParams);
            }

            vgGameBtns.setVisibility(View.GONE);
            btSignout.setVisibility(View.GONE);
            btSignIn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_signin:
                getBaseActivity().onLogIn();
                break;
            case R.id.bt_sign_out:
                getBaseActivity().onLogOut();
                break;
            case R.id.bt_single:
                showSingleModeView();
                break;
            case R.id.bt_quick:
                getBaseActivity().onStartQuickGame();
                break;
            case R.id.bt_multi:
                getBaseActivity().onStartMultiGame();
                break;
            case R.id.bt_achievement:
                getBaseActivity().onShowAchievement();
                break;
            case R.id.bt_leaderboard:
                getBaseActivity().onShowLeaderboard();
                break;
            case R.id.bt_single_4:
                getBaseActivity().onStartSingleGame(GameMode.SINGLE_4);
                break;
            case R.id.bt_single_6:
                getBaseActivity().onStartSingleGame(GameMode.SINGLE_6);
                break;
            case R.id.bt_about_us:
                showAboutUs();
        }
    }

    private void showAboutUs() {

    }

    private void showSingleModeView() {
        vgSingleSub.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onEventMainThread(PlayEvent e) {
        super.onEventMainThread(e);
        if (e.eventType == PlayEvent.EventType.LOG_IN) {
            showButtons();
        } else if (e.eventType == PlayEvent.EventType.LOG_OUT) {
            showButtons();
        }
    }
}
