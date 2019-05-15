package it.appero.esonero.myactivities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

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

        if(!getResources().getConfiguration().locale.getLanguage().equals(Locale.ITALIAN.toString())) {
            btnLang = new MyLangButton(this, layout, R.id.btnMainLang, R.drawable.itflag);
        }
        else {
            btnLang = new MyLangButton(this, layout, R.id.btnMainLang, R.drawable.enflag);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLang.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        btnExit = findViewById(R.id.btnExit);

        account = GoogleSignIn.getLastSignedInAccount(this);

        if(account != null)
        {
            Intent i = new Intent(MainActivity.this, LoggedInActivity.class);
            startActivity(i);
        }

        signInButton.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.btnExit:
                finishAffinity();
                break;
            case R.id.btnMainLang:
                if(btnLang.getLanguage() == R.drawable.itflag) {

                    String languageToLoad  = "it";
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config,this.getResources().getDisplayMetrics());

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
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

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount acc = task.getResult(ApiException.class);

                MainActivity.account = GoogleSignIn.getLastSignedInAccount(this);

                Intent i = new Intent(MainActivity.this, LoggedInActivity.class);
                startActivity(i);

            } catch (ApiException e)
                {
                }
        }
    }
}
