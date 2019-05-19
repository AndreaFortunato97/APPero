package it.appero.esonero.myviews;

import android.content.Context;
import android.util.AttributeSet;

import it.appero.esonero.R;

public class MyButton extends android.support.v7.widget.AppCompatButton {

    public MyButton(Context context) {
        super(context);
        this.setBackground(getResources().getDrawable(R.drawable.mybutton,null)); // Imposto come sfondo predefinito del bottone il 'pattern' appositamente creato ('R.drawable.mybutton')
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isInEditMode() {
        return super.isInEditMode();
    }
}