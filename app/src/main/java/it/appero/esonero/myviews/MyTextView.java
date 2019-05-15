package it.appero.esonero.myviews;

import android.content.Context;
import android.util.AttributeSet;

public class MyTextView extends android.support.v7.widget.AppCompatTextView {
    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isInEditMode() {
        return super.isInEditMode();
    }
}
