package kr.pe.kingori.ihatecolor.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import kr.pe.kingori.ihatecolor.debug.R;
import kr.pe.kingori.ihatecolor.model.Color;
import kr.pe.kingori.ihatecolor.model.GameMode;
import kr.pe.kingori.ihatecolor.ui.Constants;
import kr.pe.kingori.ihatecolor.ui.event.GameEvent;
import kr.pe.kingori.ihatecolor.ui.view.QuestionViewGroup;
import kr.pe.kingori.ihatecolor.util.SharedPreferenceUtil;
import kr.pe.kingori.ihatecolor.util.UiUtil;

import java.util.ArrayList;
import java.util.Random;

public class GameFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvCountdown;
    private QuestionViewGroup vgQuestion;
    private ViewGroup vgLives;
    private GameMode gameMode;
    private boolean clearGame;
    private SoundPool sfxPlayer;
    private MediaPlayer bgmPlayer;
    private boolean gamePaused;
    private AnimationDrawable baseBg;
    private TextView tvTimer;

    private int soundLifeDecrease;

    public static GameFragment newInstance(GameMode mode) {
        GameFragment f = new GameFragment();
        Bundle bdl = new Bundle();
        bdl.putSerializable(Constants.BDL_GAMEMODE, mode);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameMode = (GameMode) getArguments().getSerializable(Constants.BDL_GAMEMODE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initMusicPlayer();
        if (resumeMusicOnActivityCreate) {
            startMusic();
            resumeMusicOnActivityCreate = false;
        }
    }

    private void initMusicPlayer() {
        sfxPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        soundLifeDecrease = sfxPlayer.load(getActivity(), R.raw.button_33, 1);
        bgmPlayer = MediaPlayer.create(getActivity(), R.raw.autmn_road);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_game, container, false);
        vgQuestion = (QuestionViewGroup) view.findViewById(R.id.vg_question);
        vgLives = (ViewGroup) view.findViewById(R.id.vg_lives);
        tvTimer = (TextView) view.findViewById(R.id.tv_timer);
        tvCountdown = (TextView) view.findViewById(R.id.tv_countdown);


        if (gameMode == GameMode.SINGLE_4) {
            ((ViewStub) view.findViewById(R.id.vs_btn_4)).inflate();
        } else {
            ((ViewStub) view.findViewById(R.id.vs_btn_6)).inflate();
            view.findViewById(R.id.bt_black).setOnClickListener(this);
            view.findViewById(R.id.bt_orange).setOnClickListener(this);
        }
        view.findViewById(R.id.bt_red).setOnClickListener(this);
        view.findViewById(R.id.bt_blue).setOnClickListener(this);
        view.findViewById(R.id.bt_green).setOnClickListener(this);
        view.findViewById(R.id.bt_yellow).setOnClickListener(this);

        LayerDrawable layerDrawable = (LayerDrawable) view.getBackground();
        baseBg = (AnimationDrawable) layerDrawable.findDrawableByLayerId(R.id.bg_base);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!gameStarted) {
            countdown(3);
        } else if (isGamePaused()) {
            showPauseDialog();
        }
    }

    private Runnable countdownR;

    private void countdown(final int count) {
        if (!isAdded() || getBaseActivity() == null) return;
        tvCountdown.setVisibility(View.VISIBLE);

        if (count < 0) {
            tvCountdown.setVisibility(View.GONE);
            startGame();
            return;
        }

        String countText = null;
        int countColorResId = 0;
        switch (count) {
            case 3:
                countText = "3";
                countColorResId = R.color.blue;
                break;
            case 2:
                countText = "2";
                countColorResId = R.color.green;
                break;
            case 1:
                countText = "1";
                countColorResId = R.color.orange;
                break;
            case 0:
                countText = "GO";
                countColorResId = R.color.red;
                break;
        }
        tvCountdown.setText(countText);
        tvCountdown.setTextColor(getResources().getColor(countColorResId));

        countdownR = new Runnable() {
            @Override
            public void run() {
                countdown(count - 1);
            }
        };

        tvCountdown.postDelayed(countdownR, 500L);

    }

    private boolean resumeMusicOnActivityCreate = false;

    private void startMusic() {
        if (getActivity() == null) {
            resumeMusicOnActivityCreate = true;
            return;
        }
        if (sfxPlayer == null || bgmPlayer == null) {
            initMusicPlayer();
        }

        if (bgmPausePosition > 0) {
            bgmPlayer.seekTo(bgmPausePosition);
            bgmPausePosition = -1;
        }
        bgmPlayer.start();
    }

    private int curPosition = 0;
    private int curLives = 3;

    private boolean gameStarted = false;

    public boolean isGameStarted() {
        return gameStarted;
    }

    public static class Question {
        public final Color text;
        public final Color answer;

        private Question(Color answer, Color text) {
            this.answer = answer;
            this.text = text;
        }
    }

    private static final int MAX_QUESTIONS = 20;
    private static final long GAME_TIME = 30000L;

    private ArrayList<Question> questions;

    private void startGame() {
        gameStarted = true;
        prepareGame();
        if (!isGamePaused()) {
            startMusic();
            startTimer(GAME_TIME);
        }
    }

    private void prepareGame() {
        questions = buildQuestion();

        curPosition = 0;
        curLives = 3;

        vgQuestion.setQuestion(questions);
        tvTimer.setText(Integer.toString((int) GAME_TIME / 1000));
    }

    private void flashBg() {
        if (!baseBg.isRunning()) {
            baseBg.start();
        }
    }

    private CountDownTimer timer;

    private void startTimer(long totalTime) {
        timer = new CountDownTimer(totalTime, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(
                        String.format("%.2f", (float) millisUntilFinished / 1000));
                tvTimer.setTag(millisUntilFinished);
                if (millisUntilFinished < 10000) {
                    flashBg();
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setText("0");
                gameOver(false);
            }
        };
        timer.start();
    }

    private ArrayList<Question> buildQuestion() {
        ArrayList<Question> questions = new ArrayList<Question>();
        int prevRand = -1;
        int rand;
        Random random = new Random();
        Color[] colorVals = Color.values();
        int types = gameMode == GameMode.SINGLE_4 ? 4 : 6;
        while (questions.size() < MAX_QUESTIONS) {

            rand = random.nextInt(types);
            if (rand != prevRand) {
                questions.add(new Question(colorVals[rand], colorVals[random.nextInt(types)]));
                prevRand = rand;
            }
        }
        return questions;
    }

    @Override
    public void onPause() {
        super.onPause();
        onPauseGame(false);
        releasePlayer();
    }

    private int bgmPausePosition = -1;

    private void releasePlayer() {
        if (sfxPlayer != null) {
            sfxPlayer.release();
        }
        sfxPlayer = null;
        if (bgmPlayer != null) {
            bgmPausePosition = bgmPlayer.getCurrentPosition();
            bgmPlayer.release();
        }
        bgmPlayer = null;
    }

    private void pauseMusic() {
        if (bgmPlayer != null) {
            bgmPlayer.pause();
        }
    }

    private void decreaseLives() {
        curLives--;
        sfxPlayer.play(soundLifeDecrease, 1, 1, 0, 0, 1);
        if (curLives >= 0) {
            vgLives.removeViewAt(0);
        }
    }

    private void gameOver(boolean clearGame) {
        this.clearGame = clearGame;
        long elapsedTimeInMillis = GAME_TIME - (Long) tvTimer.getTag();

        processFinalScore(elapsedTimeInMillis);
        showFinishView(elapsedTimeInMillis);
        stopTimer();
        stopMusic();
        baseBg.stop();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void stopMusic() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }
    }

    private void showFinishView(long elapsedTimeInMillis) {

        if (gameMode == GameMode.MULTI) {
            String msg = "";
            if (clearGame) {
                msg = "you win. elapsed:" + elapsedTimeInMillis;
            } else if (otherPlayerStatus == OtherPlayerStatus.CLEAR) {
                msg = "you lose.";
            }

            new AlertDialog.Builder(getActivity()).setMessage(msg)
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getBaseActivity().onFinishGame();
                        }
                    }).setCancelable(false)
                    .show();
        } else {
            new AlertDialog.Builder(getActivity()).setMessage(clearGame ? ("elapsed:" + elapsedTimeInMillis / 1000) : "died")
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getBaseActivity().onFinishGame();
                        }
                    }
                    ).show();
        }
    }

    private void processFinalScore(long elapsedTime) {
        getBaseActivity().broadcastFinish(clearGame);
        if (clearGame) {
            long prevElapsedTime = SharedPreferenceUtil.getLong(Constants.SF_HIGH_SCORE);
            if (elapsedTime < prevElapsedTime) {
                UiUtil.showToast("new high score!!!");
                //TODO
//            unlock(score, prevHighScore, 50, R.string.achievement_score_50);
//            unlock(score, prevHighScore, 20, R.string.achievement_score_20);
//            unlock(score, prevHighScore, 10, R.string.achievement_score_10);

//                getBaseActivity().updateLeaderboard(score);
                SharedPreferenceUtil.put(Constants.SF_HIGH_SCORE, elapsedTime);
            }
        }
    }

    private void unlock(int score, int prevHighScore, int baseScore, int achievementId) {
        if (score >= baseScore && prevHighScore < baseScore) {
            getBaseActivity().unlockArchivement(getString(achievementId));
        }
    }

    @Override
    public void onClick(View v) {
        Color answer = null;

        switch (v.getId()) {
            case R.id.bt_red:
                answer = Color.RED;
                break;
            case R.id.bt_blue:
                answer = Color.BLUE;
                break;
            case R.id.bt_green:
                answer = Color.GREEN;
                break;
            case R.id.bt_yellow:
                answer = Color.YELLOW;
                break;
            case R.id.bt_orange:
                answer = Color.ORANGE;
                break;
            case R.id.bt_black:
                answer = Color.BLACK;
                break;
        }
        if (isRightAnswer(answer)) {
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
        vgQuestion.setCurrent(curPosition);
        if (isGameFinished()) {
            gameOver(true);
        } else {

        }
    }

    private boolean isGameFinished() {
        return curPosition >= questions.size() || curLives == 0;
    }


    private boolean isRightAnswer(Color answer) {
        return questions.get(curPosition).answer == answer;
    }


    private static enum OtherPlayerStatus {
        UNKOWN, CLEAR, DEAD
    }

    private OtherPlayerStatus otherPlayerStatus = OtherPlayerStatus.UNKOWN;

    @Override
    protected void onEventMainThread(GameEvent e) {
        switch (e.eventType) {
            case OTHER_FINISHED: {
                otherPlayerStatus = ((Boolean) e.eventVal) ? OtherPlayerStatus.CLEAR : OtherPlayerStatus.DEAD;
                if (otherPlayerStatus == OtherPlayerStatus.CLEAR) {
                    gameOver(false);
                }
            }
            break;
            case PAUSE_GAME:
                onPauseGame(false);
                break;
        }
    }


    public void onPauseGame(boolean showPauseDialog) {
        pauseMusic();
        pauseTimer();
        if (showPauseDialog) {
            showPauseDialog();
        }
        baseBg.stop();
        gamePaused = true;
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private Dialog pauseDialog;

    private void showPauseDialog() {
        if (pauseDialog == null) {
            pauseDialog = new AlertDialog.Builder(getActivity()).setTitle("game paused")
                    .setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                                        new String[]{"resume", "quit"}) {
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                onResumeGame();
                                                break;
                                            case 1:
                                                quitGame();
                                                break;
                                        }
                                    }
                                }
                    ).setCancelable(false).create();
        }
        if (!pauseDialog.isShowing()) {
            pauseDialog.show();
        }
    }

    private void onResumeGame() {
        startMusic();
        resumeTimer();
        gamePaused = false;
    }

    private void resumeTimer() {
        Long elapsedTime = (Long) tvTimer.getTag();
        startTimer(elapsedTime == null ? GAME_TIME : elapsedTime);
    }

    private void quitGame() {
        stopMusic();
        if (gameMode == GameMode.MULTI) {
            getBaseActivity().broadcastFinish(false);
        }
        getBaseActivity().onFinishGame();
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    @Override
    public void onDetach() {
        if (timer != null) {
            timer.cancel();
        }
        if (countdownR != null) {
            tvCountdown.removeCallbacks(countdownR);
        }
        super.onDetach();
    }
}
