package it.appero.esonero.myviews;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.mortbay.jetty.security.Constraint;

public class MyLangButton extends MyButton {

    ConstraintSet constraintSet = new ConstraintSet();

    Integer language;

    // Inutile
    public MyLangButton(Context context) {
        super(context);
    }

    // Inutile
    public MyLangButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Creato appositamente, così da poter passare i diversi layout delle diverse activity
    public MyLangButton(Context context, ConstraintLayout layout, int id, int lang) {
        super(context);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        if(width > 1000 && height > 1500)
            this.setLayoutParams(new ConstraintLayout.LayoutParams(125, 125));
        else
            this.setLayoutParams(new ConstraintLayout.LayoutParams(70, 70));

        this.setBackground(getResources().getDrawable(lang,null));
        this.setId(id);

        layout.addView(this);

        constraintSet.clone(layout);
        constraintSet.connect(this.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 25);
        constraintSet.connect(this.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 25);
        constraintSet.applyTo(layout);

        language = lang;

    }

    public int getLanguage() {
        return language;
    }

    @Override
    public boolean isInEditMode() {
        return super.isInEditMode();
    }
}
