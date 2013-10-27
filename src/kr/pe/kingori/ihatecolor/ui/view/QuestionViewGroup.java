package kr.pe.kingori.ihatecolor.ui.view;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import kr.pe.kingori.ihatecolor.R;
import kr.pe.kingori.ihatecolor.model.Color;
import kr.pe.kingori.ihatecolor.util.FontManager;

import java.util.ArrayList;

import static kr.pe.kingori.ihatecolor.ui.fragment.GameFragment.Question;

public class QuestionViewGroup extends RelativeLayout {

    private ArrayList<Question> question;
    private int currentPosition = -1;

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
        this.question = question;
        currentPosition = 0;
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

        int spaceBetweenItems = getResources().getDimensionPixelSize(R.dimen.question_padding);

        Color[] colors = Color.values();
        int[] colorTextWidth = new int[colors.length];
        TextPaint tp = new TextPaint(0);
        FontManager.applyTypeface(tp);
        tp.setTextSize(getResources().getDimension(R.dimen.question_text_size));
        for (int i = 0; i < colors.length; i++) {
            colorTextWidth[i] = (int) (tp.measureText(colors[i].name()) * 1.1 + 0.5) + getResources().getDimensionPixelSize(R.dimen.question_padding);
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
            widthSum += qWidth + spaceBetweenItems;
        }
        addQuestionViews(questionsInRow, measuredWidth, widthSum, currentRow, rowHeight, colorTextWidth);
        updateQestionColor();
    }

    private void addQuestionViews(ArrayList<Question> questionsInRow, int measuredWidth, int widthSum, int currentRow, int rowHeight, int[] colorTextWidth) {
        int leftMargin = (measuredWidth - widthSum) / 2;
        int padding = getResources().getDimensionPixelSize(R.dimen.question_padding);
        int spaceBetweenRows = padding /2;
        int marginTop = (rowHeight + spaceBetweenRows) * currentRow;
        for (Question question : questionsInRow) {
            CustomFontTextView tv = new CustomFontTextView(getContext());
            tv.setText(question.text.name());
            tv.setTextColor(getResources().getColor(question.answer.colorResId));
            tv.setGravity(Gravity.CENTER);
            tv.setIncludeFontPadding(false);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            tv.setPadding(0, padding, 0, padding / 2);

            int width = colorTextWidth[question.text.ordinal()];

            LayoutParams lParams = new LayoutParams(width, rowHeight);
            lParams.setMargins(leftMargin, marginTop, 0, 0);
            addView(tv, lParams);
            leftMargin += width + padding;
        }
    }

    public void setCurrent(int position) {
        currentPosition = position;
        updateQestionColor();
    }

    private void updateQestionColor() {
        int color = getResources().getColor(R.color.disable);

        for (int i = 0; i < currentPosition; i++) {
            TextView target = (TextView) getChildAt(i);
            target.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            target.setTextColor(color);
        }

        if (currentPosition >= 0 && getChildCount() > currentPosition) {
            getChildAt(currentPosition).setBackgroundColor(color);
        }
    }
}
