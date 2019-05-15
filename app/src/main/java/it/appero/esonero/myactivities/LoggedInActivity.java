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
        if(!getResources().getConfiguration().locale.getLanguage().equals(Locale.ITALIAN.toString())) {
            btnLang = new MyLangButton(this, layout, R.id.btnLoggedInLang, R.drawable.itflag);
        }
        else {
            btnLang = new MyLangButton(this, layout, R.id.btnLoggedInLang, R.drawable.enflag);
        }

        tvTitle.setText(getString(R.string.welcome));
        tvAppName.setText(getString(R.string.gpsapi));

        tvAppName.measure(0,0); // Faccio partire le misure da 0
        Shader textShader = new LinearGradient(0, (float)tvAppName.getMeasuredHeight()/2, (float)tvAppName.getMeasuredWidth(), (float)tvAppName.getMeasuredHeight()/2,
                new int[]{
                        Color.parseColor("#F97C3C"),
                        Color.parseColor("#FDB54E"),
                        Color.parseColor("#64B678"),
                        Color.parseColor("#478AEA"),
                        Color.parseColor("#8446CC"),
                }, null, Shader.TileMode.CLAMP);
        tvAppName.getPaint().setShader(textShader);
        tvAppName.setTextColor(Color.parseColor("#FFFFFF")); // Necessaria altrimenti la scritta risulta opaca

        tvInfo.setText(getString(R.string.tvInfo));
        String[] name = MainActivity.account.getDisplayName().split(" ", 3);
        tvName.setText(String.format(getString(R.string.name), name[0]));
        tvSurname.setText(String.format(getString(R.string.surname), name[1]));
        tvEmail.setText(String.format(getString(R.string.email), MainActivity.account.getEmail()));

        // Initialize the Mobile Ads SDK.
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
        Se il tasto indietro viene premuto due volte entro 2 secondi, esci
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
            case R.id.btnLogout:
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                mGoogleSignInClient.signOut();
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if(account != null)
                {
                    Toast.makeText(this, getString(R.string.logoutError), Toast.LENGTH_LONG).show();
                }
                else
                {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
            case R.id.btnLocation:
                locationMaganer = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
                if (!locationMaganer.isProviderEnabled( LocationManager.GPS_PROVIDER)) {
                    gpsAlertBox();
                }
                else {
                    Intent locationActivity = new Intent(LoggedInActivity.this, LocationActivity.class);
                    startActivity(locationActivity);
                }
                break;
            case R.id.btnLoggedInLang:
                if(btnLang.getLanguage() == R.drawable.itflag) {

                    String languageToLoad  = "it";
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config,this.getResources().getDisplayMetrics());

                    Intent intent = new Intent(LoggedInActivity.this, LoggedInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else {

                    String languageToLoad  = "en";
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config,this.getResources().getDisplayMetrics());

                    Intent intent = new Intent(LoggedInActivity.this, LoggedInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                break;
        }
    }

    private void gpsAlertBox() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.gpsError)
                .setMessage(R.string.gpsDisabled)

                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
