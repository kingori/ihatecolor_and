package kr.pe.kingori.ihatecolor.ui.fragment;

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
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.multiplayer.Participant;
import kr.pe.kingori.ihatecolor.R;
import kr.pe.kingori.ihatecolor.model.Color;
import kr.pe.kingori.ihatecolor.model.GameMode;
import kr.pe.kingori.ihatecolor.ui.Constants;
import kr.pe.kingori.ihatecolor.ui.CustomDialogFragment;
import kr.pe.kingori.ihatecolor.ui.event.DialogEvent;
import kr.pe.kingori.ihatecolor.ui.event.GameEvent;
import kr.pe.kingori.ihatecolor.ui.view.QuestionViewGroup;

import java.util.ArrayList;
import java.util.Random;

public class GameFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvCountdown;
    private QuestionViewGroup vgQuestion;
    private ViewGroup vgLives;
    private GameMode gameMode;
    private boolean gameCleared;
    private SoundPool sfxPlayer;
    private MediaPlayer bgmPlayer;
    private AnimationDrawable baseBg;
    private TextView tvTimer;
    private CountDownTimer timer;

    private int soundLifeDecrease;

    private static final int MAX_QUESTIONS = 20;
    public static final int MAX_LIFE = 3;
    private static final long GAME_TIME = 30000L;

    private Runnable countdownR;

    private ArrayList<Question> questions;

    private GameState state = GameState.COUNTDOWN;
    private int bgmPausePosition = -1;

    private int curPosition = 0;
    private int curLives = MAX_LIFE;

    private boolean resumeMusicOnActivityCreate = false;

    public boolean isGamePaused() {
        return state == GameState.PAUSE;
    }

    private enum GameState {
        COUNTDOWN, PLAYING, PAUSE, FINISH
    }

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
        int soundResId = 0;
        {
            int soundRnd = new Random().nextInt(3);
            switch (soundRnd) {
                case 2:
                    soundResId = R.raw.fall_road;
                    break;
                case 1:
                    soundResId = R.raw.umbrella;
                    break;
                case 0:
                    soundResId = R.raw.four_seasons;
                    break;
            }
        }
        bgmPlayer = MediaPlayer.create(getActivity(), soundResId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.f_game, container, false);
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
            if (gameMode == GameMode.MULTI) {
                Participant participant = getBaseActivity().getPeerInfo();
                if (participant != null) {
                    ((TextView) view.findViewById(R.id.tv_other)).setText("VS\n" + participant.getDisplayName());
                    loadUserImage((ImageView) view.findViewById(R.id.iv_other), participant);
                }
            } else {
                view.findViewById(R.id.vg_other).setVisibility(View.GONE);
            }
        }
        view.findViewById(R.id.bt_red).setOnClickListener(this);
        view.findViewById(R.id.bt_blue).setOnClickListener(this);
        view.findViewById(R.id.bt_green).setOnClickListener(this);
        view.findViewById(R.id.bt_yellow).setOnClickListener(this);

        LayerDrawable layerDrawable = (LayerDrawable) view.getBackground();
        baseBg = (AnimationDrawable) layerDrawable.findDrawableByLayerId(R.id.bg_base);

        if (savedInstanceState != null) {
            tvTimer.setTag(savedInstanceState.getLong(Constants.BDL_PLAYED_TIME, 0L));
        }

        return view;
    }

    private void loadUserImage(ImageView view, Participant participant) {
        if (participant.getIconImageUri() != null) {
            ImageManager.create(getActivity()).loadImage(view, participant.getIconImageUri());
        }
    }

    @Override
    public void onResume() {
        fragmentPaused = false;
        super.onResume();
        if (state == GameState.COUNTDOWN) {
            countdown(3);
        } else if (state == GameState.PAUSE) {
            showPauseDialog();
        } else if (state == GameState.FINISH) {
            showFinishDialog();
        }
    }

    private void countdown(final int count) {
        if (!isAdded() || getBaseActivity() == null || fragmentPaused) return;
        tvCountdown.setVisibility(View.VISIBLE);

        if (count < 0) {
            countdownR = null;
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


    public boolean isGameStarted() {
        return state != GameState.COUNTDOWN;
    }

    public static class Question {
        public final Color text;
        public final Color answer;

        private Question(Color answer, Color text) {
            this.answer = answer;
            this.text = text;
        }
    }


    private void startGame() {
        state = GameState.PLAYING;
        prepareGame();
        startMusic();
        startTimer(GAME_TIME);
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

    private boolean fragmentPaused = false;

    @Override
    public void onPause() {
        fragmentPaused = true;
        super.onPause();
        onPauseGame(false);
        releasePlayer();
    }

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
            vgLives.getChildAt(2 - curLives).setEnabled(false);
        }
    }

    private void gameOver(boolean clearGame) {
        this.gameCleared = clearGame;
        state = GameState.FINISH;

        processFinalScore();
        showFinishDialog();
        stopTimer();
        stopMusic();
        baseBg.stop();
    }

    private long getElapsedTimeInMillis() {
        return GAME_TIME - getPlayedTime();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.BDL_PLAYED_TIME, getPlayedTime());
    }

    private long getPlayedTime() {
        return tvTimer.getTag() == null ? 0L : (Long) tvTimer.getTag();
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

    private void showFinishDialog() {
        if (getFragmentManager().findFragmentByTag("diag") != null) {
            return;
        }

        String msg = "";
        long elapsedTimeInMillis = getElapsedTimeInMillis();

        if (gameCleared) {
            if (gameMode == GameMode.MULTI) {
                msg = "YOU WIN!\nCLEAR TIME: " + String.format("%.2f", (float) (elapsedTimeInMillis / 1000));
            } else {
                msg = "CLEAR!\nCLEAR TIME: " + String.format("%.2f", (float) (elapsedTimeInMillis / 1000));
            }
        } else {
            msg = "ARRGH...\nFAILED!";
        }

        CustomDialogFragment.newInstance(DialogEvent.DialogType.GAMEOVER, false, msg, "OK", null).show(getFragmentManager(), "diag");
    }

    public void onEventMainThread(DialogEvent e) {
        if (e.dialogType == DialogEvent.DialogType.GAMEOVER) {
            if (e.buttonType == DialogEvent.ButtonType.OK) {
                getBaseActivity().onFinishGame();
            }
        } else if (e.dialogType == DialogEvent.DialogType.PAUSE) {
            switch (e.buttonType) {
                case OK:
                    onResumeGame();
                    break;
                case CANCEL:
                    quitGame();
                    break;
            }
        }
    }

    private void processFinalScore() {
        getBaseActivity().broadcastFinish(gameCleared);
        getBaseActivity().submitResultToPlay(gameCleared, getElapsedTimeInMillis(), curLives, gameMode);
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
    public void onEventMainThread(GameEvent e) {
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
        if (state == GameState.PLAYING) {
            state = GameState.PAUSE;
        }
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void showPauseDialog() {
        if (getFragmentManager().findFragmentByTag("diag") == null) {
            CustomDialogFragment.newInstance(DialogEvent.DialogType.PAUSE, true, "PAUSED", "RESUME", "QUIT").show(getFragmentManager(), "diag");
        }
    }

    private void onResumeGame() {
        startMusic();
        resumeTimer();
        state = GameState.PLAYING;
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
