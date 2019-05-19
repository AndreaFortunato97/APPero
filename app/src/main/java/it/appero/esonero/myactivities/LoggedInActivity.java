package it.appero.esonero.myactivities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.Locale;

import it.appero.esonero.R;
import it.appero.esonero.myviews.MyButton;
import it.appero.esonero.myviews.MyLangButton;
import it.appero.esonero.myviews.MyTextView;

public class LoggedInActivity extends AppCompatActivity implements View.OnClickListener {

    private MyButton btnLogout, btnLocation;
    private MyTextView tvTitle, tvAppName, tvInfo, tvName, tvSurname, tvEmail;
    private MyLangButton btnLang;

    private GoogleSignInClient mGoogleSignInClient;
    /* AD PERSONALE. SERVE UN SITO WEB PER VISUALIZZARLA
    private AdView mAdView, mAdView2
    */
    private AdView mAdView3;

    private ConstraintLayout layout;

    private LocationManager locationMaganer;

    private Long timeToExit = (long) 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        tvTitle = findViewById(R.id.tvWelcome);
        tvAppName = findViewById(R.id.tvAppName);
        tvInfo = findViewById(R.id.tvInfo);
        tvName = findViewById(R.id.tvName);
        tvSurname = findViewById(R.id.tvSurname);
        tvEmail = findViewById(R.id.tvEmail);

        btnLogout = findViewById(R.id.btnLogout);
        btnLocation = findViewById(R.id.btnLocation);
        btnLogout.setOnClickListener(this);
        btnLocation.setOnClickListener(this);

        layout = findViewById(R.id.layoutLoggedIn);
        // Il seguente 'if' crea un'istanza del tasto necessario al cambio lingua
        if(!getResources().getConfiguration().locale.getLanguage().equals(Locale.ITALIAN.toString())) {
            btnLang = new MyLangButton(this, layout, R.id.btnLoggedInLang, R.drawable.itflag); // Se la lingua impostata non è 'Italiano' allora crea il tasto con la bandiera 'Italia'
        }
        else {
            btnLang = new MyLangButton(this, layout, R.id.btnLoggedInLang, R.drawable.enflag); // Se la lingua impostata è 'Italiano' allora crea il tasto con la bandiera 'Regno Unito'
        }

        tvTitle.setText(getString(R.string.welcome));
        tvAppName.setText(getString(R.string.gpsapi));

        // Il seguente blocco, fino alla riga [93], serve solamente a colorare la scritta 'Google Play Services Api'
        tvAppName.measure(0,0); // Faccio partire le 'misure' (come fosse un righello) da 0
        Shader textShader = new LinearGradient(0, (float)tvAppName.getMeasuredHeight()/2, (float)tvAppName.getMeasuredWidth(), (float)tvAppName.getMeasuredHeight()/2,
                new int[]{
                        Color.parseColor("#F97C3C"),
                        Color.parseColor("#FDB54E"),
                        Color.parseColor("#64B678"),
                        Color.parseColor("#478AEA"),
                        Color.parseColor("#8446CC"),
                }, null, Shader.TileMode.CLAMP); // Dalla riga [84] fino a questa riga creo un 'modello di pittura', cioè una sfumatura di colori
        tvAppName.getPaint().setShader(textShader); // Assegno alla MyTextView 'tvAppName' la sfumatura 'textShader' precedentemente creata
        tvAppName.setTextColor(Color.parseColor("#FFFFFF")); // Imposto un colore di base per la MyTextView 'tvAppName'

        tvInfo.setText(getString(R.string.tvInfo));
        String[] name = MainActivity.account.getDisplayName().split(" ", 3); // Prendo il nome completo assegnato all'accaount 'MainActivity.account' (precedentemente loggato) e divido il nome dal cognome con il metodo 'split'
        tvName.setText(String.format(getString(R.string.name), name[0])); // Assegno a 'tvName' il nome (presente nella prima cella dell'array 'name'
        tvSurname.setText(String.format(getString(R.string.surname), name[1])); // Assegno a 'tvSurname' il cognome (presente nella seconda cella dell'array 'name'
        tvEmail.setText(String.format(getString(R.string.email), MainActivity.account.getEmail())); // Assegno a 'tvEmail' l'email dell'account 'MainActivity.account'

        // 'Inizializzo' (collego l'account Ad Mob di Google) l'API dei GoogleAd
        MobileAds.initialize(this, "ca-app-pub-3324099742825481~3207361198");

        /* AD PERSONALE. SERVE UN SITO WEB PER VISUALIZZARLA
                mAdView = findViewById(R.id.adView);
                mAdView2 = findViewById(R.id.adView2);
        */
        mAdView3 = findViewById(R.id.adView3);
        /* AD PERSONALE. SERVE UN SITO WEB PER VISUALIZZARLA
                AdRequest adRequest = new AdRequest.Builder().build();
                AdRequest adRequest2 = new AdRequest.Builder().build();
        */
        AdRequest adRequest3 = new AdRequest.Builder().build();
        /* AD PERSONALE. SERVE UN SITO WEB PER VISUALIZZARLA
                mAdView.loadAd(adRequest);
                mAdView2.loadAd(adRequest2);
        */
        mAdView3.loadAd(adRequest3);

        btnLang.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        /*
        Se il tasto indietro viene premuto due volte entro 2 secondi, esco dall'applicazione chiudendo tutte le activity
        Altrimenti non fare nulla
        */
        if((System.currentTimeMillis() - timeToExit) <= 2000) {
            finishAffinity();
        }
        else {
            Toast.makeText(this,getString(R.string.exitMessage), Toast.LENGTH_SHORT).show();
            timeToExit = System.currentTimeMillis();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogout: // Alla pressione del tasto di Logout
                // Creo un'istanza 'GoogleSignInOptions', necessaria a configurare ciò che verrà chiesto nella riga [148] (in questo caso l'email, necessaria per effettuare il logout)
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                // Assegno ad 'mGoogleSignInClient' l'istanza 'gso' precedentemente creata
                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                // Effetto il logout dall'account
                mGoogleSignInClient.signOut();

                // 'getLastSignedInAccount' restituisce 'null' se non ci sono account collegati
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if(account != null) // Se la riga [150] non è andata a buon fine, 'account' non sarà null e quindi restituisco un errore
                {
                    Toast.makeText(this, getString(R.string.logoutError), Toast.LENGTH_LONG).show();
                }
                else // Se la riga [150] è andata a buon fine, chiudo tutto le activity e torno a quella iniziale ('MainActivity')
                {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
            case R.id.btnLocation: // Alla pressione del tasto Location
                // Assegno a 'locationManager' l'istanza di un oggetto necessario a verificare se il GPS è attivo o meno
                locationMaganer = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
                if (!locationMaganer.isProviderEnabled( LocationManager.GPS_PROVIDER)) { // Se il GPS non è attivo, chiamo il metodo 'gpsAlertBox' presente alla riga [208]
                    gpsAlertBox();
                }
                else { // Se il GPS è attivo, avvio l'activity 'LocationActivity'
                    Intent locationActivity = new Intent(LoggedInActivity.this, LocationActivity.class);
                    startActivity(locationActivity);
                }
                break;
            case R.id.btnLoggedInLang: // Alla pressione del tasto di cambio lingua
                if(btnLang.getLanguage() == R.drawable.itflag) { // Se il bottone ha la bandiera 'Italia'
                    String languageToLoad  = "it";
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale); // Imposto la lingua dell'applicazione in Italiano
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config,this.getResources().getDisplayMetrics()); // Aggiorno la configurazione (impostazione interna) dell'applicazione con la nuova lingua

                    // Riavvio l'activity
                    Intent intent = new Intent(LoggedInActivity.this, LoggedInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Prima di avviare l'activity presente come secondo parametro dell'Intent (cioè MainActivity), chiudo l'attuale. In questo caso funziona come fosse un 'riavvio'
                    startActivity(intent);
                }
                else { // Se il bottone non ha la bandiera 'Italia'
                    String languageToLoad  = "en";
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale); // Imposto la lingua dell'applicazione in Inglese
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config,this.getResources().getDisplayMetrics()); // Aggiorno la configurazione (impostazione interna) dell'applicazione con la nuova lingua

                    // Riavvio l'activity
                    Intent intent = new Intent(LoggedInActivity.this, LoggedInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Prima di avviare l'activity presente come secondo parametro dell'Intent (cioè MainActivity), chiudo l'attuale. In questo caso funziona come fosse un 'riavvio'
                    startActivity(intent);
                }

                break;
        }
    }

    private void gpsAlertBox() {
        // Creo un nuovo AlertDialog, nel quale chiedo all'utente se vuole aprire le impostazioni del telefono per attivare il GPS
        new AlertDialog.Builder(this)
                .setTitle(R.string.gpsError)
                .setMessage(R.string.gpsDisabled)
                .setCancelable(false) // Impedisco all'utente di uscire dall'AlertBox senza premere su 'Si' o 'No'
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { // Se l'utente preme 'Si', apro le impostazioni del telefono per attivare il GPS
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { // Se l'utente clicca no, non fare nulla
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Imposto come icona dell'AlertBox il triangolo di 'Attenzione'
                .show(); // Mostro l'AlertDialog appena creato
    }
}
