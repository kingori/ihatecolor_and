package kr.pe.kingori.ihatecolor.ui.event;

public class DialogEvent {

    public final DialogType dialogType;
    public final ButtonType buttonType;

    public DialogEvent(DialogType dialogType, ButtonType buttonType) {
        this.dialogType = dialogType;
        this.buttonType = buttonType;
    }

    public static enum DialogType {
        INVITATION, PAUSE, GAMEOVER
    }

    public static enum ButtonType {
        OK, CANCEL
    }

}
