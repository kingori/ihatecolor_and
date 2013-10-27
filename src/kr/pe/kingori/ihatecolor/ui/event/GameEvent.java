package kr.pe.kingori.ihatecolor.ui.event;

public class GameEvent {
    public final EventType eventType;
    public final Object eventVal;

    public static enum EventType {
        PAUSE_GAME, OTHER_FINISHED
    }

    private GameEvent(EventType eventType, Object val) {
        this.eventType = eventType;
        this.eventVal = val;
    }

    public static GameEvent newEvent(EventType eventType, Object val) {
        return new GameEvent(eventType, val);
    }
}
