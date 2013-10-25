package kr.pe.kingori.ihatecolor.ui.event;

public class GameEvent {
    public final EventType eventType;
    public final int eventVal;

    public static enum EventType {
        PAUSE_GAME, OTHER_FINISHED
    }

    private GameEvent(EventType eventType, int val) {
        this.eventType = eventType;
        this.eventVal = val;
    }

    public static GameEvent newEvent(EventType eventType, int val) {
        return new GameEvent(eventType, val);
    }
}
