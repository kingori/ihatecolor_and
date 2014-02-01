package kr.pe.kingori.ihatecolor.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.*;
import com.google.example.games.basegameutils.BaseGameActivity;
import de.greenrobot.event.EventBus;
import kr.pe.kingori.ihatecolor.R;
import kr.pe.kingori.ihatecolor.model.GameMode;
import kr.pe.kingori.ihatecolor.ui.CustomDialogFragment;
import kr.pe.kingori.ihatecolor.ui.event.DialogEvent;
import kr.pe.kingori.ihatecolor.ui.event.GameEvent;
import kr.pe.kingori.ihatecolor.ui.event.PlayEvent;
import kr.pe.kingori.ihatecolor.ui.fragment.GameFragment;
import kr.pe.kingori.ihatecolor.ui.fragment.MainFragment;
import kr.pe.kingori.ihatecolor.ui.fragment.WaitingFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseGameActivity implements
        OnInvitationReceivedListener, RoomUpdateListener, RoomStatusUpdateListener, RealTimeMessageReceivedListener {

    // tag for debug logging
    final boolean ENABLE_DEBUG = true;
    final String TAG = "kingori";

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;
    final static int RC_ACIEVEMENT = 10004;
    final static int RC_LEADERBOARD = 10006;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // Are we playing in multiplayer mode?
    GameMode gameMode;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String incomingInvitationId = null;
    private Intent intent;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[2];

    private boolean mWaitRoomDismissedFromCode = false;

    private Screen currentScreen;
    private Fragment f;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableDebugLog(ENABLE_DEBUG, TAG);

        BugSenseHandler.initAndStartSession(MainActivity.this, "9c65e78b");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        if (savedInstanceState == null && getInvitationId() == null) {
            showScreen(Screen.MAIN);
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public boolean isUserSignedIn() {
        return isSignedIn();
    }

    public void onLogOut() {
        signOut();
        EventBus.getDefault().post(PlayEvent.newEvent(PlayEvent.EventType.LOG_OUT));
    }

    public void onLogIn() {
        beginUserInitiatedSignIn();
    }

    public void onStartMultiGame() {
        intent = getGamesClient().getSelectPlayersIntent(1, 1);
        showScreen(Screen.WAITING);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    public void onFinishGame() {
        showScreen(Screen.MAIN);
    }

    public void onStartQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        showScreen(Screen.WAITING);
        keepScreenOn();
        getGamesClient().createRoom(rtmConfigBuilder.build());
    }

    public void onShowAchievement() {
        startActivityForResult(getGamesClient().getAchievementsIntent(), RC_ACIEVEMENT);
    }

    public void onShowLeaderboard() {
        startActivityForResult(getGamesClient().getAllLeaderboardsIntent(), RC_LEADERBOARD);
    }

    public void broadcastFinish(boolean clear) {
        if (GameMode.MULTI == gameMode) {
            mMsgBuf[0] = (byte) 'F';
            mMsgBuf[1] = (byte) (clear ? 1 : 0);
            for (Participant p : mParticipants) {
                if (p.getParticipantId().equals(mMyId))
                    continue;
                if (p.getStatus() != Participant.STATUS_JOINED)
                    continue;
                // final score notification must be sent via reliable message
                getGamesClient().sendReliableRealTimeMessage(null, mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
    }

    public void onStartSingleGame(GameMode mode) {
        startGame(mode);
    }

    private boolean isNearTargetTime(long elpasedTime, long targetTime) {
        long timeDiff = targetTime - elpasedTime;
        return timeDiff >= 0 && timeDiff < 10;
    }

    public void submitResultToPlay(boolean gameCleared, long elapsedTime, int curLives, GameMode gameMode) {
        switch (gameMode) {
            case SINGLE_4:
                if (gameCleared) {
                    unlockAchievement(R.string.achievement_4_color_clear);
                    if (curLives == GameFragment.MAX_LIFE) {
                        unlockAchievement(R.string.achievement_4_color_perfect_clear);
                        incrementAcheivement(R.string.achievement_4_color_perfect_clear_10_times, 1);
                        incrementAcheivement(R.string.achievement_4_color_perfect_clear_50_times, 1);
                        incrementAcheivement(R.string.achievement_4_color_perfect_clear_100_times, 1);
                    }

                    if (isNearTargetTime(elapsedTime, 9999)) {
                        unlockAchievement(R.string.achievement_4_color_clear__9_99_sec);
                    } else if (isNearTargetTime(elapsedTime, 18189)) {
                        unlockAchievement(R.string.achievement_4_color_clear__18_18_sec);
                    } else if (isNearTargetTime(elapsedTime, 29999)) {
                        unlockAchievement(R.string.achievement_4_color_clear__29_99_sec);
                    }
                    if (elapsedTime <= 5000) {
                        unlockAchievement(R.string.achievement_4_color_clear_under_5_sec);
                    }
                }
                incrementAcheivement(R.string.achievement_4_color_100_heart_consumed, GameFragment.MAX_LIFE - curLives);
                break;
            case SINGLE_6:
                if (gameCleared) {
                    unlockAchievement(R.string.achievement_6_color_clear);
                    if (curLives == GameFragment.MAX_LIFE) {
                        unlockAchievement(R.string.achievement_6_color_perfect_clear);
                        incrementAcheivement(R.string.achievement_6_color_perfect_clear_10_times, 1);
                        incrementAcheivement(R.string.achievement_6_color_perfect_clear_50_times, 1);
                        incrementAcheivement(R.string.achievement_6_color_perfect_clear_100_times, 1);
                    }
                    if (isNearTargetTime(elapsedTime, 9999)) {
                        unlockAchievement(R.string.achievement_6_color_clear__9_99_sec);
                    } else if (isNearTargetTime(elapsedTime, 18189)) {
                        unlockAchievement(R.string.achievement_6_color_clear__18_18_sec);
                    } else if (isNearTargetTime(elapsedTime, 29999)) {
                        unlockAchievement(R.string.achievement_6_color_clear__29_99_sec);
                    }
                    if (elapsedTime <= 5000) {
                        unlockAchievement(R.string.achievement_6_color_clear_under_5_sec);
                    }
                }
                incrementAcheivement(R.string.achievement_6_color_100_heart_consumed, GameFragment.MAX_LIFE - curLives);
                break;
            case MULTI:
                break;
        }

        if (gameCleared) {
            updateLeaderboard(gameMode, elapsedTime);
        }
    }

    public void showAboutUs() {
        Dialog aboutUsDialog = new Dialog(this, android.R.style.Theme_Holo_NoActionBar);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uriString = "https://plus.google.com/";
                switch (v.getId()) {
                    case R.id.vg_josh:
                        uriString += "+JoshHaNyungChung";
                        break;
                    case R.id.vg_kingori:
                        uriString += "+SewonAnn";
                        break;
                    case R.id.vg_ben:
                        uriString += "+KyungjaeHa";
                        break;
                }

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uriString)));
            }
        };
        aboutUsDialog.setContentView(R.layout.diag_about_us);
        aboutUsDialog.findViewById(R.id.vg_josh).setOnClickListener(listener);
        aboutUsDialog.findViewById(R.id.vg_kingori).setOnClickListener(listener);
        aboutUsDialog.findViewById(R.id.vg_ben).setOnClickListener(listener);
        aboutUsDialog.show();

    }

    private static enum Screen {
        MAIN, WAITING, ERROR, GAME
    }

    private void showScreen(Screen screen) {
        currentScreen = screen;

        Fragment prevFragment = f;
        f = null;
        switch (screen) {
            case MAIN:
                f = new MainFragment();
                break;
            case ERROR:
                f = new MainFragment();
                break;
            case WAITING:
                f = new WaitingFragment();
                break;
            case GAME:
                f = GameFragment.newInstance(gameMode);
                break;
        }

        if (screen == Screen.ERROR) {
            Toast.makeText(this, "UNKNOWN ERROR", Toast.LENGTH_SHORT).show();
        }

        if (f != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (prevFragment != null) {
                ft.remove(prevFragment);
            }
            ft.replace(R.id.fragment_content, f, "screen").commitAllowingStateLoss();
        }
        setInvitationViewVisibility();
    }

    @Override
    public void onSignInFailed() {
        // Sign in has failed. So show the user the sign-in button.
        EventBus.getDefault().post(PlayEvent.newEvent(PlayEvent.EventType.LOG_OUT));
    }


    @Override
    public void onSignInSucceeded() {
        // show sign-out button, hide the sign-in button
        EventBus.getDefault().post(PlayEvent.newEvent(PlayEvent.EventType.LOG_IN));

        getGamesClient().registerInvitationListener(this);

        // if we received an invite via notification, accept it; otherwise, go
        // to main screen
        if (getInvitationId() != null) {
            acceptInviteToRoom(getInvitationId());
            return;
        }
    }


    private void acceptInviteToRoom(String invitationId) {
        Log.d(TAG, "Accepting invitation: " + invitationId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invitationId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        showScreen(Screen.WAITING);
        keepScreenOn();
        getGamesClient().joinRoom(roomConfigBuilder.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        // if we're in a room, leave it.
        leaveRoom();
        stopKeepingScreenOn();
        super.onStop();
    }

    private void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            getGamesClient().leaveRoom(this, mRoomId);
            mRoomId = null;
            showScreen(Screen.WAITING);
        } else {
            showScreen(Screen.MAIN);
        }
    }

    public void onEventMainThread(DialogEvent e) {
        if (e.dialogType == DialogEvent.DialogType.INVITATION) {
            switch (e.buttonType) {
                case OK:
                    acceptInviteToRoom(incomingInvitationId);
                    incomingInvitationId = null;
                    break;
                case CANCEL:
                    break;
            }
        }
    }

    private Participant inviter;

    @Override
    public void onInvitationReceived(Invitation invitation) {
        incomingInvitationId = invitation.getInvitationId();
        inviter = invitation.getInviter();
        setInvitationViewVisibility();
    }

    @Override
    public void onInvitationRemoved(String s) {

    }

    private void setInvitationViewVisibility() {
        boolean showInvitationPopup = false;
        if (incomingInvitationId != null) {
            if (gameMode == GameMode.MULTI) {
                showInvitationPopup = (currentScreen == Screen.MAIN);
            } else {
                showInvitationPopup = (currentScreen == Screen.MAIN || currentScreen == Screen.GAME);
            }
        }
        if (showInvitationPopup) {
            CustomDialogFragment
                    .newInstance(DialogEvent.DialogType.INVITATION, true,
                            getString(R.string.invitation, inviter.getDisplayName()), getString(R.string.accept), getString(R.string.deny))
                    .show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesClient.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showScreen(Screen.ERROR);
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        mWaitRoomDismissedFromCode = false;

        // minimum number of players required for our game
        final int MIN_PLAYERS = 2;
        Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesClient.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showScreen(Screen.ERROR);
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        showScreen(Screen.MAIN);
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesClient.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showScreen(Screen.ERROR);
            return;
        }
        updateRoom(room);
    }

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onPeerJoined(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(getGamesClient().getCurrentPlayerId());

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showScreen(Screen.ERROR);
    }

    @Override
    public void onPeersConnected(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> strings) {
        updateRoom(room);
    }

    private void updateRoom(Room room) {
        mParticipants = room.getParticipants();
    }

    public Participant getPeerInfo() {
        if (mParticipants != null) {
            for (Participant participant : mParticipants) {
                if (!participant.getParticipantId().equals(mMyId)) {
                    return participant;
                }
            }
        }
        return null;
    }

    @Override
    public void onP2PConnected(String s) {
        //?
    }

    @Override
    public void onP2PDisconnected(String s) {
        //?
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();
        if (buf[0] == 'F' || buf[0] == 'U') {
            // score update.
            boolean otherCleared = ((int) buf[1]) == 1;
            EventBus.getDefault().post(GameEvent.newEvent(GameEvent.EventType.OTHER_FINISHED, otherCleared));
        } else if (buf[0] == 'S') {
            // someone else started to play -- so dismiss the waiting room and
            // get right to it!
            Log.d(TAG, "Starting game because we got a start message.");

            dismissWaitingRoom();
            startGame(GameMode.MULTI);
        }
    }

    private void dismissWaitingRoom() {
        mWaitRoomDismissedFromCode = true;
        finishActivity(RC_WAITING_ROOM);
    }


    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, data);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(responseCode, data);
                break;
            case RC_WAITING_ROOM:
                // ignore result if we dismissed the waiting room from code:
                if (mWaitRoomDismissedFromCode) break;

                // we got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // player wants to start playing
                    Log.d(TAG, "Starting game because user requested via waiting room UI.");

                    // let other players know we're starting.
                    broadcastStart();

                    // start the game!
                    startGame(GameMode.MULTI);
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player actively indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    /* Dialog was cancelled (user pressed back key, for
                     * instance). In our game, this means leaving the room too. In more
                     * elaborate games,this could mean something else (like minimizing the
                     * waiting room UI but continue in the handshake process). */
                    leaveRoom();
                }

                break;
        }
    }

    private void broadcastStart() {
        if (GameMode.MULTI != gameMode) {
            return; // playing single-player mode
        }

        mMsgBuf[0] = 'S';
        mMsgBuf[1] = (byte) 0;
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            getGamesClient().sendReliableRealTimeMessage(null, mMsgBuf, mRoomId,
                    p.getParticipantId());
        }
    }

    private void startGame(GameMode mode) {
        gameMode = mode;
        showScreen(Screen.GAME);
    }

    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            showScreen(Screen.MAIN);
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(GamesClient.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            showScreen(Screen.MAIN);
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
        if (invitees != null) {
            Log.d(TAG, "Invitee count: " + invitees.size());
        }

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        if (invitees != null) {
            rtmConfigBuilder.addPlayersToInvite(invitees);
        }
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        showScreen(Screen.WAITING);
        keepScreenOn();
        getGamesClient().createRoom(rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    public void unlockAchievement(int resId) {
        if (isSignedIn()) {
            getGamesClient().unlockAchievement(getString(resId));
        } else {
            //TODO
        }
    }

    public void incrementAcheivement(int resId, int incVal) {
        if (incVal > 0 && isSignedIn()) {
            getGamesClient().incrementAchievement(getString(resId), incVal);
        }
    }

    public void updateLeaderboard(GameMode mode, long elapsedTime) {
        if (isSignedIn()) {
            getGamesClient().submitScore(getString(mode == GameMode.SINGLE_4 ? R.string.leaderboard_4_colors : R.string.leaderboard_6_colors), elapsedTime);
        } else {
            //TODO
        }
    }

    @Override
    public void onBackPressed() {
        if (currentScreen == Screen.GAME) {
            GameFragment gameF = (GameFragment) f;
            if (!gameF.isGameStarted()) {
                showScreen(Screen.MAIN);
            } else if (gameF.isGamePaused()) {
                finish();
            } else {
                ((GameFragment) f).onPauseGame(true);
            }
        } else if (currentScreen == Screen.WAITING) {
            showScreen(Screen.MAIN);
            dismissWaitingRoom();
        } else {
            finish();
        }

    }
}
