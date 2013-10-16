package kr.pe.kingori.peoplematcher.ui.event;

public class PlayEvent {
    public final EventType eventType;

    public static enum EventType {
        LOG_IN, LOG_OUT
    }

    private PlayEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public static PlayEvent newEvent(EventType eventType) {
        return new PlayEvent(eventType);
    }
}
