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
        if(!getResources().getConfiguration().locale.getLanguage().equals(Locale.ITALIAN.toString())) {
            btnLang = new MyLangButton(this, layout, R.id.btnLocationLang, R.drawable.itflag);
        }
        else {
            btnLang = new MyLangButton(this, layout, R.id.btnLocationLang, R.drawable.enflag);
        }

        btnLang.setOnClickListener(this);

        permissions.add(ACCESS_FINE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);

        if(!permissionsToRequest.isEmpty()) {
            Toast.makeText(this, getString(R.string.grantPerm), Toast.LENGTH_LONG).show();
            requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), 777);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            refreshInfo(location);
                        }
                    }
                });

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);

        btnMaps.setOnClickListener(this);
    }

    protected void refreshInfo(Location location) {

        tvLocation.setText(String.format(getString(R.string.latlon), location.getLatitude(), location.getLongitude()));
        precision = location.getAccuracy();
        tvPrecision.setText(String.format(getString(R.string.precision), metConv(precision), centConv(precision)));
        tvProvider.setText((String.format(getString(R.string.provider), location.getProvider())));

        if(speed > 0.2) {
            tvSpeed.setText(String.format(getString(R.string.speed), speed, speed*0.32));
        }
        else {
            tvSpeed.setText(String.format(getString(R.string.speed), 0.00, 0.00));
        }

        altitude = location.getAltitude();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvAltitude.setText(String.format(getString(R.string.altitudeOreo), metConv(altitude), centConv(altitude), location.getVerticalAccuracyMeters()));
        }
        else {
            tvAltitude.setText(String.format(getString(R.string.altitude), metConv(altitude), centConv(altitude)));
        }

        lat = location.getLatitude();
        lon = location.getLongitude();
    }

    private int metConv(double meters) {
        return (int) meters;
    }

    private int centConv(double meters) {
        Double floatMeters = meters - (long) meters;

        return (int)(floatMeters*100);
    }

    protected void onStart() {
        super.onStart();

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        if (apiAvailability.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            flagPlayServices = false;
            tvLocation.setText(getString(R.string.installGPS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //while()
        if(!flagPlayServices)
            startLocationUpdates();
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        Boolean hasMarshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        if (hasMarshmallow) {
            return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

            // This is Case 1 again as Permission is not granted by user

            //Now further we check if used denied permanently or not
            if (shouldShowRequestPermissionRationale(permissions[0])) {
                // case 4 User has denied permission but not permanently
                Toast.makeText(this, R.string.grantPerm, Toast.LENGTH_LONG).show();
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), 777);

            } else {
                // case 5. Permission denied permanently.
                // You can open Permission setting's page from here now.

                new AlertDialog.Builder(this)
                        .setTitle(R.string.permError)
                        .setMessage(R.string.permDenied)
                        .setCancelable(false)
                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent); // Visualizza la pagina 'Settings->Permissions'
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    refreshInfo(location);

                    if (lastLocation != null) {
                        secsFromLastLocation = ((location.getTime() - lastLocation.getTime()) / 1000);
                        speed = location.distanceTo(lastLocation) / secsFromLastLocation;
                    }
                    lastLocation = location;
                }
            }
        },null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMaps:
                if(!(lat == 0.0 || lon==0.0)) {
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(String.format(getString(R.string.mapsUrl), Double.toString(lat), Double.toString(lon)))));
                }
                else {
                    Toast.makeText(this, getString(R.string.locError), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnLocationLang:
                if(btnLang.getLanguage() == R.drawable.itflag) {

                    String languageToLoad  = "it";
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config,this.getResources().getDisplayMetrics());

                    Intent intent = new Intent(LocationActivity.this, LocationActivity.class);
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

                    Intent intent = new Intent(LocationActivity.this, LocationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
        }

    }
}
