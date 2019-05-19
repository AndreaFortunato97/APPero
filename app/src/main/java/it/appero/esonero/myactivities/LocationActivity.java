package it.appero.esonero.myactivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Locale;

import it.appero.esonero.R;
import it.appero.esonero.myviews.MyButton;
import it.appero.esonero.myviews.MyLangButton;
import it.appero.esonero.myviews.MyTextView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class LocationActivity extends AppCompatActivity implements View.OnClickListener{

    private MyTextView tvLocation,tvPrecision,tvAltitude,tvProvider,tvSpeed;
    private MyButton btnMaps;
    private MyLangButton btnLang;

    private Double lat = 0.0, lon = 0.0, altitude;
    private double speed = 0, secsFromLastLocation;
    private Float precision;

    private FusedLocationProviderClient fusedLocationClient;

    private LocationRequest mLocationRequest;
    private Location lastLocation = null;

    private ArrayList permissionsToRequest;
    private ArrayList permissions = new ArrayList();

    private boolean flagPlayServices;

    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        tvLocation = findViewById(R.id.tvLocation);
        tvPrecision = findViewById(R.id.tvPrecision);
        tvAltitude = findViewById(R.id.tvAltitude);
        tvProvider = findViewById(R.id.tvProvider);
        tvSpeed = findViewById(R.id.tvSpeed);

        btnMaps = findViewById(R.id.btnMaps);

        layout = findViewById(R.id.layoutLocation);
        // Il seguente 'if' crea un'istanza del tasto necessario al cambio lingua
        if(!getResources().getConfiguration().locale.getLanguage().equals(Locale.ITALIAN.toString())) {
            btnLang = new MyLangButton(this, layout, R.id.btnLocationLang, R.drawable.itflag); // Se la lingua impostata non è 'Italiano' allora crea il tasto con la bandiera 'Italia'
        }
        else {
            btnLang = new MyLangButton(this, layout, R.id.btnLocationLang, R.drawable.enflag); // Se la lingua impostata è 'Italiano' allora crea il tasto con la bandiera 'Regno Unito'
        }

        btnLang.setOnClickListener(this);

        // Aggiungo all'array 'permissions' tutti i permessi da chiedere (in questo caso quelli relativi alla posizione)
        permissions.add(ACCESS_FINE_LOCATION);

        // Chiamo il metodo 'findUnAskedPermissions', con i permessi da chiedere, che mi restituirà i permessi non ancora concessi
        permissionsToRequest = findUnAskedPermissions(permissions);

        // Se la lista di permessi da chiedere contiene qualcosa, allora richiedo tali permessi
        if(!permissionsToRequest.isEmpty()) {
            Toast.makeText(this, getString(R.string.grantPerm), Toast.LENGTH_LONG).show();
            requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), 777); // Chiedo i permessi presenti nell'array 'permissionsToRequest' ed entro nel metodo 'onRequestPermissionsResult' presente alla riga [206]
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // Creo un oggetto 'fusedLocationClient' necessario ad ottenere informazioni sulla posizione
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Se l'utente rifiutasse qualche permesso, 'return' => 'Ferma' il processo
        }
        fusedLocationClient.getLastLocation() // Prendo le informazioni sull'ultima posizione rilevata
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // 'SE' l'operazione va a buon fine (senza errori), entro nel metodo 'addOnSuccessListener'
                        if (location != null) {
                            refreshInfo(location); // Se l'ultima posizione rilevata è diversa da 'null' (cioè esiste) allora chiama il metodo refreshInfo
                        }
                    }
                });

        // Creo un oggetto 'mLocationRequest' per ottenere informazioni sulla posizione attuale (non l'ultima rilevata)
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Imposto la priorità di tale richiesta su 'ALTA' (cioè PRIMA fai questo, POI continua l'esecuzione)
        mLocationRequest.setInterval(2000); // Aggiorno la posizione MASSIMO ogni 2000 ms (2 secondi)
        mLocationRequest.setFastestInterval(1000); // Aggiorno la posizione MINIMO ogni 1000 ms (1 secondo)

        btnMaps.setOnClickListener(this);
    }

    protected void refreshInfo(Location location) {

        tvLocation.setText(String.format(getString(R.string.latlon), location.getLatitude(), location.getLongitude())); // Ottengo Latitudine e Longitudine della posizione attuale
        precision = location.getAccuracy(); // Ottengo la precisione della posizione, che indica il RAGGIO (misurato in metri) nel quale potresti essere
        tvPrecision.setText(String.format(getString(R.string.precision), metConv(precision), centConv(precision))); // 'Scompongo' il raggio in metri (metodo 'metConv') e centimetri (metodo 'centConv')
        tvProvider.setText((String.format(getString(R.string.provider), location.getProvider()))); // Ottengo il 'provider' del GPS (quale servizio è utilizzato per ottenere la posizione. In questo caso SEMPRE 'fused' perche utilizziamo l'API FusedLocationProviderClient)

        // La velocità subisce delle alterazioni 'statistiche' perchè il GPS può erroneamente rilevare dei cambiamenti di posizione, che corrispondono quindi ad un errato incremento della velocità
        if(speed > 0.2) { // Impostando la velocità superiore a 0.2 elimino, con alta percentuale, i possibili errori generati dal GPS
            tvSpeed.setText(String.format(getString(R.string.speed), speed, speed*0.32)); // La misurazione della velocità è affidabile al 68%, quindi aggiungo/tolgo un margine pari al 32%
        }
        else { // Se la velocità risulta inferiore a 0.2, con elevata probabilità si tratta di un errore e quindi la 'forzo' a 0
            tvSpeed.setText(String.format(getString(R.string.speed), 0.00, 0.00));
        }

        altitude = location.getAltitude(); // Ottiengo l'altitudine

        // Dall'API 26 (Oreo) in su posso ottenere anche la precisione (misurata in metri) dell'altitudine
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Controllo se è installata una versione di Android pari o superiore ad Oreo: in caso affermativo ottengo anche la precisione dell'altitudine
            tvAltitude.setText(String.format(getString(R.string.altitudeOreo), metConv(altitude), centConv(altitude), location.getVerticalAccuracyMeters())); // Scompongo l'altidudine in metri (metodo 'metConv') e centimetri (metodo 'centConv')
        }
        else {
            tvAltitude.setText(String.format(getString(R.string.altitude), metConv(altitude), centConv(altitude))); // Scompongo l'altidudine in metri (metodo 'metConv') e centimetri (metodo 'centConv')
        }

        // Assegno a 'lat' e 'lon' rispettivamente la Latitudine e la Longitudine, necessarie a calcolare la velocità (riga [])
        lat = location.getLatitude();
        lon = location.getLongitude();
    }

    // Preso un valore reale, ne estrapolo la parte intera (es: 5,26 -> 5)
    private int metConv(double meters) {
        return (int) meters;
    }

    // Preso un valore reale, ne estrapolo la parte decimale (es: 5,26 -> 26)
    private int centConv(double meters) {
        Double floatMeters = meters - (long) meters;

        return (int)(floatMeters*100);
    }

    protected void onStart() {
        super.onStart();

        // Creo un'istanza dell'oggetto 'GoogleApiAvailability', necessaria al controllo della presenza delle API Google (Google Play Services)
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        if (apiAvailability.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) { // Se sul dispositivo NON sono disponibili i Google Play Services, stampo un messaggio d'errore
            flagPlayServices = false;
            tvLocation.setText(getString(R.string.installGPS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Appena viene aperta/riaperta (torna in foreground) l'activity, inizia l'update sistematico della posizione (ogni 1-2 secondi, impostati alle righe [114] e [115])
        if(!flagPlayServices)
            startLocationUpdates();
    }

    // Metodo necessario alla ricerca dei permessi non ancora concessi
    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) { // Per ogni permesso presente nell'array 'wanted', contenente tutti i permessi necessari all'applicazione
            if (!hasPermission(perm)) { // Chiamo il metodo 'hasPermission' presente alla riga [196], per la verifica del permesso 'perm'
                result.add(perm); // Se l'utente non ha ancora concesso il permesso 'perm', lo aggiungo alla lista dei permessi da chiedere
            }
        }
        return result;
    }

    // Metodo necessario a verificare se il permesso 'permission' è stato già accettato o meno
    private boolean hasPermission(String permission) {
        Boolean hasMarshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M; // Prima di tutto controllo se la versione di Android è ALMENO Marshmallow
        if (hasMarshmallow) { // Se la versione di Android è ALMENO Marshmallow, chiamo il metodo 'checkSelfPermission' e controllo se il permesso 'permission' è stato già accettato ('PERMISSION_GRANTED')
            return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    // Questo metodo viene chiamato OGNI volta che l'utente accetta o rifiuta un permesso (richiesto alla riga [93]
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            // Caso nel quale l'utente non ha ancora accettato i permessi

            if (shouldShowRequestPermissionRationale(permissions[0])) {
                // Caso nel quale l'utente ha rifiutato i permessi SENZA selezionare la checkbox 'Non mostrare più'/'Non chiedere più'
                Toast.makeText(this, R.string.grantPerm, Toast.LENGTH_LONG).show(); // Mostro un messaggio all'utente, chiedendogli di nuovo di garantire i permessi rifiutati
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), 777); // Richiedo i permessi rifiutati

            } else {
                // Caso nel quale l'utente ha rifiutato i permessi SELEZIONANDO ANCHE la checkbox 'Non mostrare più'/'Non chiedere più'

                // Creo un nuovo AlertBox, nel quale chiedo all'utente se vuole aprire le impostazioni dell'applicazione per garantire i permessi precedentemente rifiutati in modo 'permanente'
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permError)
                        .setMessage(R.string.permDenied)
                        .setCancelable(false) // Impedisco all'utente di uscire dall'AlertBox senza premere su 'Si' o 'No'
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { // Se l'utente preme 'Si', apro le impostazioni dell'applicazione per garantire i permessi necessari
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { // Se l'utente clicca no, termina l'activity e torna a quella precedente (LoggedInActivity)
                                finish();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) { // Se l'utente dovesse 'chiudere' l'AlertBox tramite il tasto 'Back' del telefono, termina l'activity e torna a quella precedente (LoggedInActivity
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert) // Imposto come icona dell'AlertBox il triangolo di 'Attenzione'
                        .show(); // Mostro l'AlertDialog appena creato
            }
        }
    }


    private void startLocationUpdates() {
        // Se l'utente non ha accettato i permessi, esci senza fare altro
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Chiedo al 'fusedLocationClient' di aggiornare la posizione
        fusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    refreshInfo(location); // Ogni volta che la posizione cambia, richiamo il metodo 'refreshInfo' presente alla riga [120] per l'aggiornamento di tutte le MyTextViews

                    if (lastLocation != null) { // Se prima della posizione attuale ('location') ce n'è un'altra ('lastLocation')
                        secsFromLastLocation = ((location.getTime() - lastLocation.getTime()) / 1000); // Controllo quanti secondi sono passati dalla rilevazione dell'ultima posizione
                        speed = location.distanceTo(lastLocation) / secsFromLastLocation; // Calcolo la velocità come spazio/tempo (spazio='distanceTo', tempo='secsFromLastLocation')
                    }
                    lastLocation = location; // Aggiorno l'ultima posizione rilevata con quella attuale
                }
            }
        },null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMaps: // Alla pressione del tasto Maps
                if(!(lat == 0.0 || lon == 0.0)) { // Se 'latitudine' e 'longitudine' sono entrambe diverse da 0 allora apro l'applicazione maps, con la posizione centrata esattamente su latitudine 'lat' e longitudine 'lon'
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(String.format(getString(R.string.mapsUrl), Double.toString(lat), Double.toString(lon)))));
                }
                else { // In caso contrario stampo un messaggio di errore
                    Toast.makeText(this, getString(R.string.locError), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnLocationLang: // Alla pressione del tasto di cambio lingua
                if(btnLang.getLanguage() == R.drawable.itflag) { // Se il bottone ha la bandiera 'Italia'
                    String languageToLoad  = "it";
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale); // Imposto la lingua dell'applicazione in Italiano
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config,this.getResources().getDisplayMetrics()); // Aggiorna la configurazione (impostazione interna) dell'applicazione con la nuova lingua

                    Intent intent = new Intent(LocationActivity.this, LocationActivity.class);
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
                    Intent intent = new Intent(LocationActivity.this, LocationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Prima di avviare l'activity presente come secondo parametro dell'Intent (cioè MainActivity), chiudo l'attuale. In questo caso funziona come fosse un 'riavvio'
                    startActivity(intent);
                }
                break;
        }

    }
}
