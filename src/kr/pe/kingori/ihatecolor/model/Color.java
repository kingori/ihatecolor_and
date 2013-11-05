package kr.pe.kingori.ihatecolor.model;

import kr.pe.kingori.ihatecolor.R;

public enum Color {
    RED(R.color.red, R.string.red), BLUE(R.color.blue, R.string.blue),
    GREEN(R.color.green, R.string.green), YELLOW(R.color.yellow, R.string.yellow),
    ORANGE(R.color.orange, R.string.orange), BLACK(R.color.black, R.string.black);
    public final int colorResId;
    public final int nameResId;

    Color(int colorResId, int nameResId) {
        this.colorResId = colorResId;
        this.nameResId = nameResId;
    }
}
