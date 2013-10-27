package kr.pe.kingori.ihatecolor.ui.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import de.greenrobot.event.EventBus;
import kr.pe.kingori.ihatecolor.ui.activity.MainActivity;
import kr.pe.kingori.ihatecolor.ui.event.GameEvent;
import kr.pe.kingori.ihatecolor.ui.event.PlayEvent;

public abstract class BaseFragment extends Fragment {

    protected MainActivity getBaseActivity() {
        return (MainActivity) getActivity();
    }

    public void onEventMainThread(PlayEvent e) {

    }

    public void onEventMainThread(GameEvent e) {

    }

    @Override
    public void onAttach(Activity activity) {
        EventBus.getDefault().register(this);
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }
}
