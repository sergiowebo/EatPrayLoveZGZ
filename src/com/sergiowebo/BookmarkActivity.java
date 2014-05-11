package com.sergiowebo;
 
import java.util.ArrayList;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.sergiowebo.bbdd.DBHelper;
import com.sergiowebo.bbdd.MapLocation;

 
public class BookmarkActivity extends FragmentActivity implements LocationListener, OnMapClickListener, OnMarkerClickListener  {
 
	// Mapa
    GoogleMap googleMap;
 	
 	// Etiqueta para el LOG
    private static final String LOG_TAG = "ZGZCityGuide";
  
    // Objeto que nos ayuda con la localizacion
    LocationManager locationManager;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        
        // Comprobamos si Google Play esta instalado
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services no esta disponible
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
 
        }else { // Google Play Services esta disponible
        	setUpMapIfNeeded();
        }
    }
    
	/**
	 * Inicializa el Mapa que utilizaremos para mostrar 
	 */
    private void setUpMapIfNeeded() {
        if (googleMap == null) {
        	// Version a partir de API Level 11
        		//mapa = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        	// Version igual o anterior a API Level 10
        	googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (googleMap != null) {
            	
            	// Habilitamos MyLocation de Google Map
                googleMap.setMyLocationEnabled(true);
     
                // Obtenemos LocationManager de System Service LOCATION_SERVICE
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
     
                // Creamos el objeto criteria para obtener el proveedor
                Criteria criteria = new Criteria();
     
                // Obtenemos el nombre del mejor proveedor
                String provider = locationManager.getBestProvider(criteria, true);
     
                // Obtenemos nuestra localizacion
                Location location = locationManager.getLastKnownLocation(provider);
     
                if(location!=null){
                    onLocationChanged(location);
                }
                locationManager.requestLocationUpdates(provider, 20000, 0, this);
            	
                initializeUI();

            }
        }
    }
    
    
    @Override
    public void onLocationChanged(Location location) {
  
        // Obtiene la latitud de nuestra posicion actual
        double latitude = location.getLatitude();
 
        // Obtiene la lomgitud de nuestra posicion actual
        double longitude = location.getLongitude();
 
        // Crea el objeto LatLng para la posicion actual
        LatLng latLng = new LatLng(latitude, longitude);
 
        // Muestra nuestra posicion actual en el mapa
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
 
        // Zoom en el mapa
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }
 
    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mapa, menu);
        return true;
    }
    
  
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {	
		switch(item.getItemId()){
			case R.id.vista_normal:
				googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
			case R.id.vista_hybrid:
				googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				break;
			case R.id.vista_satellite:
				googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;
			case R.id.vista_terrain:
				googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				break;
			case R.id.menu_posicion:
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(provider);
				Toast.makeText(BookmarkActivity.this, 
						"Lat: " + location.getLatitude() + " - Lng: " + location.getLongitude(), 
						Toast.LENGTH_LONG).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
    private void initializeUI() {
        googleMap.setOnMapClickListener(this);
        addSavedLocations();
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLong) {
        DialogAddPlace dialogAddPlace = new DialogAddPlace(this, true, false);
        dialogAddPlace.setLatLng(latLong);
        dialogAddPlace.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                DialogAddPlace d = (DialogAddPlace) dialog;
                addLocation(d.getLocation());
            }
        });
        dialogAddPlace.show();
    }

    /**
     * Obtiene de la BBDD los marcadores que tenemos guardadamos
     */
    private void addSavedLocations() {
        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();
        DBHelper helper = new DBHelper(getApplicationContext());
        helper.open();
        ArrayList<MapLocation> locations = helper.getSavedLocations(visibleRegion);
        helper.close();
        for (MapLocation location : locations) {
            addLocation(location);
        }
    }

    /**
     * Creo los marker en el mapa para sitio guardado en la BBDD
     * @param location
     */
    private void addLocation(MapLocation location) {
        if (location != null) {
            MarkerOptions placeMarker = new MarkerOptions();
            placeMarker.title(location.getName());
            placeMarker.snippet(location.getDescription());
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            placeMarker.position(latLng);
            googleMap.addMarker(placeMarker);
        }
    }

    /**
     * Cuando pulsamos sobre uno de los marcadores
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        boolean value = false;
        Log.i(getClass().getName(), marker.getTitle());
        DBHelper helper = new DBHelper(getApplicationContext());
        helper.open();
        MapLocation location = helper.getLocationByName(marker.getTitle());
        helper.close();
        
        // Si el marcador existe mostramos la ventana
        if (location != null) {
            View myContentsView = getLayoutInflater().inflate(R.layout.custom_info_content, null);
            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
               tvTitle.setText(location.getName());
               TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
               tvSnippet.setText(location.getDescription());
        }
        return value;
    }
	
}