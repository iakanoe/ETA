package com.winoe.eta;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    enum ActualLocReq {
        INICIAL, SETLOCENABLED, CHECKLOC
    }
    ActualLocReq actualLocReq;
    GoogleApiClient mGoogleApiClient;
    Location locActual;
    GoogleMapOptions options;
    static final LatLngBounds CABABounds = new LatLngBounds(new LatLng(-34.771753, -58.593714), new LatLng(-34.501140, -58.272363));
    GoogleMap mapa, mapReq;
    boolean boolReqs;
    boolean booleanReq; //para las request del setMyLocationEnabled(GoogleMap, boolean)


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pedirPermisos();
    }
    void initializeDesign() {
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);

                //TODO handle navigation

                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        Button nav_btn = (Button) findViewById(R.id.nav_btn);
        nav_btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        crearApiClient();
    }
    void crearApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }
        mGoogleApiClient.connect();
    }
    @Override public void onConnected(Bundle connectionHint) {
        generarOpciones();
    }
    @Override public void onConnectionSuspended(int i) {
    }
    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    void generarOpciones() {
        options = new GoogleMapOptions();
        locActual = checkLoc();
        options.camera(new CameraPosition(new LatLng(locActual.getLatitude(), locActual.getLongitude()), 12, 0, 0));
        options.compassEnabled(true);
        options.latLngBoundsForCameraTarget(CABABounds);
        options.mapType(GoogleMap.MAP_TYPE_NORMAL);
        options.rotateGesturesEnabled(true);
        options.scrollGesturesEnabled(true);
        options.tiltGesturesEnabled(true);
        options.zoomControlsEnabled(false);
        options.zoomGesturesEnabled(true);
        options.mapToolbarEnabled(false);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
        mapFragment.newInstance(options);
    }
    @Override public void onMapReady(GoogleMap map1) {
        mapa = map1;
        configMap();
    }
    void configMap() {
        mapa.setBuildingsEnabled(false);
        mapa.setIndoorEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mapReq = mapa;
            booleanReq = true;
            boolReqs = true;
            reqLocPerms(ActualLocReq.SETLOCENABLED);
        }
        mapa.setMyLocationEnabled(true);
        Location locX = checkLoc();
        mapa.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(locX.getLatitude(), locX.getLongitude())));
        mapa.setTrafficEnabled(false);
        mapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mapa.addMarker(new MarkerOptions().position(latLng).title("Marcador Colocado"));
            }
        });
    }


    void reqLocPerms(ActualLocReq a) {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        actualLocReq = a;
    }
    Location checkLoc() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            boolReqs = true;
            reqLocPerms(ActualLocReq.CHECKLOC);
        }
        return /*(mGoogleApiClient != null ?*/ LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)/* : null)*/;
    }
    void pedirPermisos() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            boolReqs = true;
            reqLocPerms(ActualLocReq.CHECKLOC);
        } else initializeDesign();
    }
    void cierraApp() {
        finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    @Override protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}
