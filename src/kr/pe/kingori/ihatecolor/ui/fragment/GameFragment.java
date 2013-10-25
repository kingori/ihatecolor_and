package kr.pe.kingori.ihatecolor.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import kr.pe.kingori.ihatecolor.debug.R;
import kr.pe.kingori.ihatecolor.ui.Constants;
import kr.pe.kingori.ihatecolor.ui.event.GameEvent;
import kr.pe.kingori.ihatecolor.ui.view.QuestionViewGroup;
import kr.pe.kingori.ihatecolor.util.SharedPreferenceUtil;
import kr.pe.kingori.ihatecolor.util.UiUtil;

import java.util.ArrayList;
import java.util.Random;

public class GameFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvScore;
    private QuestionViewGroup vgQuestion;
    private TextView tvLives;
    private int score = 0;
    private boolean isMultiplayer = false;
    private boolean clearGame;
    private SoundPool sfxPlayer;
    private MediaPlayer bgmPlayer;
    private boolean paused;

    private int soundLifeDecrease;
    private TextView tvTimer;

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
        tvScore = (TextView) view.findViewById(R.id.tv_score);
        vgQuestion = (QuestionViewGroup) view.findViewById(R.id.vg_question);
        tvLives = (TextView) view.findViewById(R.id.tv_lives);
        tvTimer = (TextView) view.findViewById(R.id.tv_timer);

        view.findViewById(R.id.bt_red).setOnClickListener(this);
        view.findViewById(R.id.bt_blue).setOnClickListener(this);
        view.findViewById(R.id.bt_green).setOnClickListener(this);
        view.findViewById(R.id.bt_yellow).setOnClickListener(this);

        updateScore(0);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!gameStarted) {
            startGame();
        }
        startMusic();
    }

    private boolean resumeMusicOnActivityCreate = false;

    private void startMusic() {
        if (getActivity() == null) {
            resumeMusicOnActivityCreate = true;
            return;
        }
        if (sfxPlayer == null) {
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

    public static enum Color {
        RED(android.graphics.Color.parseColor("#ff0000")), BLUE(android.graphics.Color.parseColor("#0000ff")),
        GREEN(android.graphics.Color.parseColor("#00ff00")), YELLOW(android.graphics.Color.parseColor("#ffff00")),
        ORANGE(android.graphics.Color.parseColor("#ffa500")), PURPLE(android.graphics.Color.parseColor("#800080"));
        public final int color;

        Color(int color) {
            this.color = color;
        }
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

    private ArrayList<Question> questions;

    private void startGame() {
        gameStarted = true;

        questions = buildQuestion();

        curPosition = 0;
        curLives = 3;

        vgQuestion.setQuestion(questions);
        updateLives();
        startMusic();
        startTimer(30000);
    }

    private CountDownTimer timer;

    private void startTimer(long totalTime) {
        timer = new CountDownTimer(totalTime, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(
                        String.format("%.2f", (float) millisUntilFinished / 1000));
                tvTimer.setTag(millisUntilFinished);
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
        while (questions.size() < MAX_QUESTIONS) {
            rand = random.nextInt(4);
            if (rand != prevRand) {
                questions.add(new Question(colorVals[rand], colorVals[random.nextInt(4)]));
                prevRand = rand;
            }
        }
        return questions;
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    private int bgmPausePosition = -1;

    private void releasePlayer() {
        sfxPlayer.release();
        sfxPlayer = null;
        bgmPausePosition = bgmPlayer.getCurrentPosition();
        bgmPlayer.release();
        bgmPlayer = null;
    }

    private void pauseMusic() {
        bgmPlayer.pause();
    }

    private void updateScore(int newScore) {
        score = newScore;
        tvScore.setText("score: " + score);
    }

    private void decreaseLives() {
        curLives--;
        sfxPlayer.play(soundLifeDecrease, 1, 1, 0, 0, 1);
        updateLives();
    }

    private void updateLives() {
        tvLives.setText("lives: " + curLives);
    }

    private void gameOver(boolean clearGame) {
        this.clearGame = clearGame;
        processFinalScore();
        showFinishView();
        stopTimer();
        stopMusic();
    }

    private void stopTimer() {
        timer.cancel();
    }

    private void stopMusic() {
        bgmPlayer.stop();
    }

    private ProgressDialog progDiag;

    private void showFinishView() {
        if (!isMultiplayer) {
            new AlertDialog.Builder(getActivity()).setMessage("final score:" + score + "\nyou " + (clearGame ? "cleared" : "died"))
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    getBaseActivity().onFinishGame();
                }
            }).show();
        } else {
            if (otherPlayerScore < 0) {
                progDiag = new ProgressDialog(getActivity());
                progDiag.setMessage("waiting for other player to finish");
                progDiag.setIndeterminate(true);
                progDiag.setCancelable(false);
                progDiag.show();
            } else {
                if (progDiag != null) {
                    progDiag.dismiss();
                    progDiag = null;
                }
                new AlertDialog.Builder(getActivity()).setMessage("final score:" + score + "\nyou " + (clearGame ? "cleared" : "died") + "\nother player:" + otherPlayerScore)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        getBaseActivity().onFinishGame();
                    }
                }).show();
            }
        }
    }

    private void processFinalScore() {
        getBaseActivity().broadcastScore(score);

        int prevHighScore = SharedPreferenceUtil.getInt(Constants.SF_HIGH_SCORE);
        if (score > prevHighScore) {
            UiUtil.showToast("new high score!!!");
            //TODO
//            unlock(score, prevHighScore, 50, R.string.achievement_score_50);
//            unlock(score, prevHighScore, 20, R.string.achievement_score_20);
//            unlock(score, prevHighScore, 10, R.string.achievement_score_10);

            getBaseActivity().updateLeaderboard(score);
            SharedPreferenceUtil.put(Constants.SF_HIGH_SCORE, score);
        }
        tvScore.setText("your score:" + score);
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
        vgQuestion.setSolved(curPosition);
        curPosition++;

        if (isGameFinished()) {
            gameOver(true);
        }
    }

    private boolean isGameFinished() {
        return curPosition >= questions.size() || curLives == 0;
    }


    private boolean isRightAnswer(Color answer) {
        return questions.get(curPosition).answer == answer;
    }

    private int otherPlayerScore = -1;

    @Override
    protected void onEventMainThread(GameEvent e) {
        switch (e.eventType) {
            case OTHER_FINISHED: {
                otherPlayerScore = e.eventVal;
                if (isGameFinished()) {
                    showFinishView();
                } else {
                    Toast.makeText(getActivity(), "other player finished game", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case PAUSE_GAME:
                onPauseGame();
                break;
        }
    }


    public void onPauseGame() {
        pauseMusic();
        pauseTimer();
        showPauseDialog();
        paused = true;
    }

    private void pauseTimer() {
        timer.cancel();
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
        paused = false;
    }

    private void resumeTimer() {
        startTimer((Long) tvTimer.getTag());
    }

    private void quitGame() {
        stopMusic();
        if (isMultiplayer) {
            getBaseActivity().broadcastScore(score);
        }
        getBaseActivity().onFinishGame();
    }

    public boolean isPaused() {
        return paused;
    }
}
