package kr.pe.kingori.ihatecolor.model;

import kr.pe.kingori.ihatecolor.R;

public enum Color {
    RED(R.color.red), BLUE(R.color.blue),
    GREEN(R.color.green), YELLOW(R.color.yellow),
    ORANGE(R.color.orange), BLACK(R.color.black);
    public final int colorResId;

    Color(int colorResId) {
        this.colorResId = colorResId;
    }
}
