package it.appero.esonero.myactivities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;

import java.util.Locale;

import it.appero.esonero.R;
import it.appero.esonero.myviews.MyButton;
import it.appero.esonero.myviews.MyLangButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private GoogleSignInClient mGoogleSignInClient;

    private SignInButton signInButton;
    private MyButton btnExit;
    private MyLangButton btnLang;
    public static GoogleSignInAccount account;

    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.layoutMain);

        // Il seguente 'if' crea un'istanza del tasto necessario al cambio lingua
        if(!getResources().getConfiguration().locale.getLanguage().equals(Locale.ITALIAN.toString())) {
            btnLang = new MyLangButton(this, layout, R.id.btnMainLang, R.drawable.itflag); // Se la lingua impostata non è 'Italiano' allora crea il tasto con la bandiera 'Italia'
        }
        else {
            btnLang = new MyLangButton(this, layout, R.id.btnMainLang, R.drawable.enflag); // Se la lingua impostata è 'Italiano' allora crea il tasto con la bandiera 'Regno Unito'
        }

        // Creo un'istanza 'GoogleSignInOptions', necessaria a configurare ciò che verrà chiesto nella riga (in questo caso l'email) [56]
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Assegno ad 'mGoogleSignInClient' l'istanza 'gso' precedentemente creata
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLang.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD); // Imposto la dimensione del tasto generato dalla libreria 'SignInButton' proprietaria di Google
        btnExit = findViewById(R.id.btnExit);

        account = GoogleSignIn.getLastSignedInAccount(this); // Assegno ad 'account' l'eventuale ultimo account Google con il quale è stato fatto l'accesso

        if(account != null) { // Se precedentemente è stato fatto l'accesso con un account Google, entra nell'if ed avvia l'activity 'LoggedInActivity'
            Intent i = new Intent(MainActivity.this, LoggedInActivity.class);
            startActivity(i);
        }

        signInButton.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button: // Alla pressione del tasto di Login
                signIn(); // Esegui la funzione 'signIn()', presente alla riga [121]
                break;
            case R.id.btnExit: // Alla pressione del tasto di uscita
                finishAffinity(); // Chiudi tutte le activity, comprese quelle in stato di 'Pausa' e 'Stop'
                break;
            case R.id.btnMainLang: // Alla pressione del tasto di cambio lingua
                if(btnLang.getLanguage() == R.drawable.itflag) { // Se il bottone ha la bandiera 'Italia'
                    String languageToLoad  = "it";
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale); // Imposta la lingua dell'applicazione in Italiano
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config,this.getResources().getDisplayMetrics()); // Aggiorna la configurazione (impostazione interna) dell'applicazione con la nuova lingua

                    // Riavvio l'activity
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Prima di avviare l'activity presente come secondo parametro dell'Intent (cioè MainActivity), chiudo l'attuale. In questo caso funziona come fosse un 'riavvio'
                    startActivity(intent);
                }
                else { // Se il bottone non ha la bandiera 'Italia'
                    String languageToLoad  = "en";
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale); // Imposta la lingua dell'applicazione in Inglrse
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config,this.getResources().getDisplayMetrics());

                    // Riavvio l'activity
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Prima di avviare l'activity presente come secondo parametro dell'Intent (cioè MainActivity), chiudo l'attuale. In questo caso funziona come fosse un 'riavvio'
                    startActivity(intent);
                }

                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1); // Avvia l'activity gestita da Google, per effettuare il Login tramite email (richiesta alla riga [53]
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // Entro in questa funziona quando esco dall'activity di Login creata alla riga [123]
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) { // Risultato ritornato dall'avvio dell'intent 'signInIntent' (riga [123]), con requestCode pari a 1
            try { // Il 'try' e' di sicurezza, in caso 'getLastSignInAccount dovesse fallire

                // Assegna alla variabile 'account' quello con il quale è appena stato effettuato il login
                MainActivity.account = GoogleSignIn.getLastSignedInAccount(this);

                // Avvia l'activity 'LoggedInActivity'
                Intent i = new Intent(MainActivity.this, LoggedInActivity.class);
                startActivity(i);

            } catch (Exception e)
                {
                    // Se per quale motivo il 'getLastSignedInAccount' dovesse fallire, ritorna all'utente un messaggio d'errore
                    Toast.makeText(this, getString(R.string.loginError), Toast.LENGTH_LONG).show();
                }
        }
    }
}
