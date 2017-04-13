package com.winoe.eta;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.renderscript.RenderScript;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, NavigationView.OnNavigationItemSelectedListener {
    enum ActualLocReq {
        INICIAL
    }
    ActualLocReq actualLocReq;
    GoogleApiClient mGoogleApiClient;
    Location myLocation;
    static final LatLngBounds CABABounds = new LatLngBounds(new LatLng(-34.771753, -58.593714), new LatLng(-34.501140, -58.272363));
    GoogleMap mapa;
    boolean boolReqs, seguirLoc, autoMove = false;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pedirPermisos();
        initializeDesign();
    }
    void initializeDesign(){
        LinearLayout ll = (LinearLayout) findViewById(R.id.persistentSearch);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ll.getLayoutParams());
        lp.setMargins(16, (16+getStatusBarHeight()), 16, 16);
        ll.setLayoutParams(new FrameLayout.LayoutParams(lp));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);
        findViewById(R.id.nav_btn).setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { ((DrawerLayout) findViewById(R.id.drawer)).openDrawer(GravityCompat.START); }});
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                goToLocation(myLocation);
                seguirLoc = true;
                setFABColor((FloatingActionButton) findViewById(R.id.fab), R.color.fabTintSiguiendo);
            }
        });
        crearApiClient();
    }
    void setFABColor(FloatingActionButton f, int color){
        int[][] states = new int[][] {new int[] { android.R.attr.state_enabled}, new int[] {-android.R.attr.state_enabled}, new int[] {-android.R.attr.state_checked}, new int[] { android.R.attr.state_pressed}};
        int[] colors = new int[] {color, color, color, color};
        f.setImageTintList(new ColorStateList(states, colors));
    }
    @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);

        //TODO handle navigation

        ((DrawerLayout) findViewById(R.id.drawer)).closeDrawers();
        return true;
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
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    @Override public void onConnected(Bundle connectionHint) {
        Log.println(Log.ASSERT, null, "conectado");
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
        MapFragment.newInstance(new GoogleMapOptions()
                .compassEnabled(true)
                .mapType(GoogleMap.MAP_TYPE_NORMAL)
                .rotateGesturesEnabled(true)
                .scrollGesturesEnabled(true)
                .tiltGesturesEnabled(true)
                .zoomControlsEnabled(false)
                .zoomGesturesEnabled(true)
                .mapToolbarEnabled(false));
    }
    @Override public void onConnectionSuspended(int i) {
    }
    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override public void onMapReady(GoogleMap map1) {
        mapa = map1;
        mapa.setBuildingsEnabled(true);
        mapa.setIndoorEnabled(true);
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) pedirPermisos();
        mapa.setMyLocationEnabled(true);
        mapa.getUiSettings().setMyLocationButtonEnabled(false);
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        setSiguiendo(true);
        mapa.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {@Override public void onCameraMoveStarted(int i) {if(!autoMove) setSiguiendo(false);}});
        mapa.setTrafficEnabled(false);
        mapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {@Override public void onMapLongClick(LatLng latLng){mapa.addMarker(new MarkerOptions().position(latLng).title("Marcador Colocado"));}});
    }
    void reqLocPerms(ActualLocReq a) {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        actualLocReq = a;
    }
    void pedirPermisos() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            boolReqs = true;
            reqLocPerms(ActualLocReq.INICIAL);
        }
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
    @Override public void onLocationChanged(Location location) {
        myLocation = location;
        if(seguirLoc) goToLocation(location);
    }
    void goToLocation(Location loc){
        autoMove = true;
        mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 18));
        autoMove = false;
    }
    void setSiguiendo(Boolean value){
        seguirLoc = value;
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) pedirPermisos();
        int color = (value ? R.color.fabTintSiguiendo : R.color.fabTintNormal);
        int[][] states = new int[][] {new int[] { android.R.attr.state_enabled}, new int[] {-android.R.attr.state_enabled}, new int[] {-android.R.attr.state_checked}, new int[] { android.R.attr.state_pressed}};
        int[] colors = new int[] {color, color, color, color};
        ((FloatingActionButton) findViewById(R.id.fab)).setImageTintList(new ColorStateList(states, colors));
        if(value) goToLocation(myLocation);
    }
}