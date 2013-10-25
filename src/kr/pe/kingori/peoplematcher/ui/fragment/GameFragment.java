package kr.pe.kingori.peoplematcher.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    private int score = 0;
    private boolean isMultiplayer = false;
    private boolean clearGame;
    private SoundPool sfxPlayer;
    private MediaPlayer bgmPlayer;
    private boolean paused;

    private int soundLifeDecrease;

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
        tvQuestion = (TextView) view.findViewById(R.id.tv_question);
        tvLives = (TextView) view.findViewById(R.id.tv_lives);

        view.findViewById(R.id.bt_red).setOnClickListener(this);
        view.findViewById(R.id.bt_blue).setOnClickListener(this);
        view.findViewById(R.id.bt_green).setOnClickListener(this);

        updateScore(0);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!gameStarted) {
            prepareQuestion();
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

    private int[] answers = new int[]{2, 1, 0};
    private int curPosition = 0;
    private int curLives = 3;

    private boolean gameStarted = false;

    private void prepareQuestion() {
        gameStarted = true;
        tvQuestion.setText("green blue red");
        curPosition = 0;
        curLives = 3;
        updateLives();
        startMusic();
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
        stopMusic();
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
            unlock(score, prevHighScore, 50, R.string.achievement_score_50);
            unlock(score, prevHighScore, 20, R.string.achievement_score_20);
            unlock(score, prevHighScore, 10, R.string.achievement_score_10);

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
        showPauseDialog();
        paused = true;
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
        paused = false;
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
