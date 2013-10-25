package kr.pe.kingori.ihatecolor.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import kr.pe.kingori.ihatecolor.debug.R;
import kr.pe.kingori.ihatecolor.ui.fragment.GameFragment;

import java.util.ArrayList;

import static kr.pe.kingori.ihatecolor.ui.fragment.GameFragment.Question;

public class QuestionViewGroup extends RelativeLayout {

    public QuestionViewGroup(Context context) {
        super(context);
    }

    public QuestionViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setQuestion(final ArrayList<Question> question) {
        if (getMeasuredWidth() > 0) {
            layoutQuestion(question, getMeasuredWidth());
        } else {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layoutQuestion(question, getMeasuredWidth());
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
    }

    private void layoutQuestion(ArrayList<Question> question, int measuredWidth) {
        removeAllViews();

        GameFragment.Color[] colors = GameFragment.Color.values();
        int[] colorTextWidth = new int[colors.length];
        TextView tv = new TextView(getContext());
        tv.setTextSize(getResources().getDimensionPixelSize(R.dimen.question_text_size));
        TextPaint tp = new TextPaint(0);
        tp.setTextSize(getResources().getDimensionPixelSize(R.dimen.question_text_size));
        for (int i = 0; i < colors.length; i++) {
            colorTextWidth[i] = (int) (tp.measureText(colors[i].name()) + 0.5) + getResources().getDimensionPixelSize(R.dimen.question_padding);
        }

        int currentRow = 0;
        int widthSum = 0;
        int rowHeight = getResources().getDimensionPixelSize(R.dimen.question_height);
        ArrayList<Question> questionsInRow = new ArrayList<Question>();
        Question q;
        for (int i = 0; i < question.size(); i++) {
            q = question.get(i);
            int qWidth = colorTextWidth[q.text.ordinal()];
            if (widthSum + qWidth >= measuredWidth) {
                addQuestionViews(questionsInRow, measuredWidth, widthSum, currentRow, rowHeight, colorTextWidth);
                questionsInRow.clear();
                widthSum = 0;
                currentRow++;
            }
            questionsInRow.add(q);
            widthSum += qWidth;
        }
        addQuestionViews(questionsInRow, measuredWidth, widthSum, currentRow, rowHeight, colorTextWidth);
    }

    private void addQuestionViews(ArrayList<Question> questionsInRow, int measuredWidth, int widthSum, int currentRow, int rowHeight, int[] colorTextWidth) {
        int leftMargin = (measuredWidth - widthSum) / 2;
        int marginTop = rowHeight * currentRow;
        for (Question question : questionsInRow) {
            TextView tv = new CustomFontTextView(getContext());
            tv.setText(question.text.name());
            tv.setTextColor(question.answer.color);
            tv.setGravity(Gravity.CENTER);

            int width = colorTextWidth[question.text.ordinal()];

            LayoutParams lParams = new LayoutParams(width, rowHeight);
            lParams.setMargins(leftMargin, marginTop, 0, 0);
            addView(tv, lParams);
            leftMargin += width;
        }
    }

    private int colorSolved = Color.parseColor("#c0c0c0");

    public void setSolved(int position) {
        ((TextView) getChildAt(position)).setTextColor(colorSolved);
    }
}
