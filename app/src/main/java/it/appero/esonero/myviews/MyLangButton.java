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

    // Costruttore creato appositamente, così da poter passare come parametri i diversi layout delle diverse activity
    public MyLangButton(Context context, ConstraintLayout layout, int id, int lang) {
        super(context);

        // Con il seguente blocco di codice ottengo le dimensioni dello schermo in pixel (es 1440x2150)
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        if(width > 1000 && height > 1500) // Se la risoluzione dello schermo è 'abbastanza' alta, creo il bottone di una certa grandezza
            this.setLayoutParams(new ConstraintLayout.LayoutParams(125, 125));
        else // Altrimenti lo creo più piccolo
            this.setLayoutParams(new ConstraintLayout.LayoutParams(70, 70));

        this.setBackground(getResources().getDrawable(lang,null)); // Imposto lo sfondo (e quindi il bottone stesso) in base alla lingua 'lang' passata come parametro
        this.setId(id); // Imposto l'ID del bottone in base al parametro 'id' passato come parametro

        layout.addView(this); // Aggiungo il bottone al layout 'layout' passato come parametro

        constraintSet.clone(layout); // Clono i 'collegamenti' (vincoli) del layout 'layout'
        // Sposto il bottone in modo che venga posizionato in alto a destra dello schermo, distanziandolo di soli 25px dai bordi
        constraintSet.connect(this.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 25); // Collego il margine destro del bottone al bordo destro dello schermo
        constraintSet.connect(this.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 25); // Collego il margine superiore del bottone al bordo superiore dello schermo
        constraintSet.applyTo(layout); // Applico il nuovo 'constraintSet', comprensivo di tutti i precedenti collegamenti PIU' quelli relativi al nuovo bottone

        language = lang; // Aggiorno la lingua del bottone con quella passata come parametro
    }

    // Metodo che restituisce solamente la lingua attuale del bottone
    public int getLanguage() {
        return language;
    }

    @Override
    public boolean isInEditMode() {
        return super.isInEditMode();
    }
}
