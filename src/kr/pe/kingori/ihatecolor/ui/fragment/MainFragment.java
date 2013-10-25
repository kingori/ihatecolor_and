package kr.pe.kingori.ihatecolor.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import kr.pe.kingori.ihatecolor.debug.R;
import kr.pe.kingori.ihatecolor.ui.event.PlayEvent;

public class MainFragment extends BaseFragment implements View.OnClickListener {
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_main, container, false);
        view.findViewById(R.id.sign_in_button).setOnClickListener(this);
        view.findViewById(R.id.sign_out_button).setOnClickListener(this);
        view.findViewById(R.id.single_player).setOnClickListener(this);
        view.findViewById(R.id.quick_game).setOnClickListener(this);
        view.findViewById(R.id.multi_player).setOnClickListener(this);
        view.findViewById(R.id.bt_achievement).setOnClickListener(this);
        view.findViewById(R.id.bt_leaderboard).setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        showButtons();
    }

    private void showButtons() {
        boolean loggedIn = getBaseActivity().isUserSignedIn();
        view.findViewById(R.id.sign_in_button).setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.sign_out_button).setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.single_player).setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.quick_game).setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.multi_player).setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.bt_achievement).setVisibility(loggedIn?View.VISIBLE :View.GONE);
        view.findViewById(R.id.bt_leaderboard).setVisibility(loggedIn?View.VISIBLE :View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                getBaseActivity().onLogIn();
                break;
            case R.id.sign_out_button:
                // sign out.
                getBaseActivity().onLogOut();
                break;
            case R.id.single_player:
                getBaseActivity().onStartSingleGame();
                break;
            case R.id.quick_game:
                getBaseActivity().onStartQuickGame();
                break;
            case R.id.multi_player:
                getBaseActivity().onStartMultiGame();
                break;
            case R.id.bt_achievement:
                getBaseActivity().onShowAchievement();
                break;
            case R.id.bt_leaderboard:
                getBaseActivity().onShowLeaderboard();
        }
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
