package kr.pe.kingori.peoplematcher.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import kr.pe.kingori.peoplematcher.R;
import kr.pe.kingori.peoplematcher.ui.Constants;
import kr.pe.kingori.peoplematcher.ui.event.GameEvent;
import kr.pe.kingori.peoplematcher.util.SharedPreferenceUtil;
import kr.pe.kingori.peoplematcher.util.UiUtil;

public class GameFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvScore;
    private TextView tvQuestion;
    private TextView tvLives;
    private TextView tvFinish;
    private int score = 0;
    private boolean isMultiplayer = false;
    private boolean clearGame;

    public static GameFragment newInstance(boolean multiplayer) {
        GameFragment f = new GameFragment();
        Bundle bdl = new Bundle();
        bdl.putBoolean(Constants.BDL_IS_MULTIPLAYER, multiplayer);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMultiplayer = getArguments().getBoolean(Constants.BDL_IS_MULTIPLAYER, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_game, container, false);
        tvScore = (TextView) view.findViewById(R.id.tv_score);
        tvQuestion = (TextView) view.findViewById(R.id.tv_question);
        tvLives = (TextView) view.findViewById(R.id.tv_lives);
        tvFinish = (TextView) view.findViewById(R.id.tv_finish);
        tvFinish.setOnClickListener(this);

        view.findViewById(R.id.bt_red).setOnClickListener(this);
        view.findViewById(R.id.bt_blue).setOnClickListener(this);
        view.findViewById(R.id.bt_green).setOnClickListener(this);

        updateScore(0);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareQuestion();
    }

    private int[] answers = new int[]{2, 1, 0};
    private int curPosition = 0;
    private int curLives = 3;

    private void prepareQuestion() {
        tvQuestion.setText("green blue red");
        curPosition = 0;
        curLives = 4;
        decreaseLives();
    }

    private void updateScore(int newScore) {
        score = newScore;
        tvScore.setText("score: " + score);
    }

    private void decreaseLives() {
        curLives--;
        tvLives.setText("lives: " + curLives);
    }

    private void gameOver(boolean clearGame) {
        this.clearGame = clearGame;
        processFinalScore();
        updateFinishView();
    }

    private void updateFinishView() {
        if (!isMultiplayer) {
            tvFinish.setText("final score:" + score + "/ you " + (clearGame ? "cleared" : "died"));
            tvFinish.setClickable(true);
        } else {
            if (otherPlayerScore < 0) {
                tvFinish.setText("final score:" + score + "/ waiting other player to finish");
                tvFinish.setClickable(false);
            } else {
                tvFinish.setText("final score:" + score + "/ other player:" + otherPlayerScore);
                tvFinish.setClickable(true);
            }
        }
        tvFinish.setVisibility(View.VISIBLE);
    }

    private void processFinalScore() {
        getBaseActivity().broadcastScore(score);

        int prevHighScore = SharedPreferenceUtil.getInt(Constants.SF_HIGH_SCORE);
        if (score > prevHighScore) {
            UiUtil.showToast("new high score!!!");
            unlock(score, prevHighScore, 50, R.string.achievement_score_50);
            unlock(score, prevHighScore, 20, R.string.achievement_score_20);
            unlock(score, prevHighScore, 10, R.string.achievement_score_10);

            getBaseActivity().updateLeaderboard(score);
        }

        SharedPreferenceUtil.put(Constants.SF_HIGH_SCORE, score);

        tvScore.setText("your score:" + score);
    }

    private void unlock(int score, int prevHighScore, int baseScore, int achievementId) {
        if (score >= baseScore && prevHighScore < baseScore) {
            getBaseActivity().unlockArchivement(getString(achievementId));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_finish) {
            getBaseActivity().onFinishGame();
            return;
        }

        int answer = 0;

        switch (v.getId()) {
            case R.id.bt_red:
                answer = 0;
                break;
            case R.id.bt_blue:
                answer = 1;
                break;
            case R.id.bt_green:
                answer = 2;
        }
        if (isRightAnswer(answer)) {
            score += 1;
            updateScore(score);
            progressToNextQuestion();
        } else {
            decreaseLives();
            if (isGameFinished()) {
                gameOver(false);
            }
        }
    }

    private void progressToNextQuestion() {
        curPosition++;

        if (isGameFinished()) {
            gameOver(true);
        }
    }

    private boolean isGameFinished() {
        return curPosition >= answers.length || curLives == 0;
    }


    private boolean isRightAnswer(int answer) {
        return answers[curPosition] == answer;
    }

    private int otherPlayerScore = -1;

    @Override
    protected void onEventMainThread(GameEvent e) {
        if (e.eventType == GameEvent.EventType.OTHER_FINISHED) {
            otherPlayerScore = e.eventVal;
            if (isGameFinished()) {
                updateFinishView();
            } else {
                Toast.makeText(getActivity(), "other player finished game", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
