package com.sergiowebo;
 
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
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
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sergiowebo.bbdd.MapLocation;
import com.sergiowebo.util.CoordinateConversion;

 
public class EatingActivity extends FragmentActivity implements LocationListener {
 
	// Mapa
    GoogleMap googleMap;
    
	// TAG para el log
    private static final String LOG_TAG = "ZGZCityGuide";
    
    // URL del WebService
    private static final String SERVICE_URL = "http://www.zaragoza.es/georref/json/hilo/ver_Restaurante";
    
    // Nos proporciona nuestra localizacion
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
     
                if(location!=null)
                    onLocationChanged(location);
                
                locationManager.requestLocationUpdates(provider, 20000, 0, this);
            	
                // Contruimos nuestros objetos en el mapa
                setUpMap();
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
    
    /**
     * Obtenemos los datos del web service
     */
    private void setUpMap() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    obtenDataWebService();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "No se ha recogido informacion del WebService", e);
                    return;
                }
            }
        }).start();
    }

    /**
     * Lee el resultado del WebService en formato JSON
     * y envia el objeto a un metodo que lo parsea e introduce en nuestro mapa
     * 
     * @throws IOException
     */
    protected void obtenDataWebService() throws IOException {
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            // Creamos la conexion con el web serice
            URL url = new URL(SERVICE_URL);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
 
            // Leemos el JSON y lo guardamos en un String
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to service", e);
            throw new IOException("Error connecting to service", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
 
        // Creamos los marcadores para el resultado
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    creaMarcadoresData(json.toString());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error procesando el JSON", e);
                }
            }
        });
    }
    
    /**
     * Crea marcadores para cada objeto dentro del JSON
     * Formato del JSON:
     * .....
     * ....features [
     * 		properties{
     * 			title:
     * 			description:
     * 		}
     * 		geometry:{
     * 			coordenadas:
     * 		}
     * ]
     * @param json
     * @throws JSONException
     */
    void creaMarcadoresData(String json) throws JSONException {
        // De-serializa el JSON string, lo lee y va creando marcadores para cada registro
        JSONObject jObj = new JSONObject(json);
        JSONArray jsonArray = jObj.getJSONArray("features");

        for (int i = 0; i < jsonArray.length(); i++) {
            // Creamos los marcadores
            JSONObject jsonObj = jsonArray.getJSONObject(i);                
        	JSONObject jsonObjProperties = jsonObj.getJSONObject("properties");
            JSONArray jsonArrayGeometry = jsonObj.getJSONObject("geometry").getJSONArray("coordinates");

            // Realizamos la conversacion entre coordenadas UTM y Lat Lon
            double[] latLon;
    		CoordinateConversion convertCoordenadas = new CoordinateConversion();
    		
    		// Especificamos la zona 30 T Para Zaragoza
    		latLon = convertCoordenadas.utm2LatLon("30 T "
    												+ jsonArrayGeometry.getDouble(0) + " "
    												+ jsonArrayGeometry.getDouble(1));
            
    		// Aplicamos un factor de correcion porque la informacion proporciona no es exacta
    		googleMap.addMarker(new MarkerOptions()
            	.title(jsonObjProperties.getString("title"))
                .snippet(jsonObjProperties.getString("description"))
                .position(new LatLng(
                		latLon[0]-0.002,
                		latLon[1]-0.0015))
            );
        }
        
        // Definimos el custom info content cuando queremos ver la informacion de cada marker
        googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
			
			@Override
			public View getInfoWindow(Marker marker) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public View getInfoContents(Marker marker) {
                View myContentsView = getLayoutInflater().inflate(R.layout.custom_info_content, null);
                TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
                   tvTitle.setText(marker.getTitle());
                   TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
                   tvSnippet.setText(marker.getSnippet());
               return myContentsView;
           }
		});
        
        
        // Definimos que sucedera cuando el usuario pulse sobre el infoWindowAdapter
        googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO Auto-generated method stub
	            DialogAddPlace addPlaceDialog = new DialogAddPlace(EatingActivity.this, false, true);
	            MapLocation location = new MapLocation();
		            location.setDescription(marker.getSnippet());
		            location.setName(marker.getTitle());
		            location.setLatitude(marker.getPosition().latitude);
		            location.setLongitude(marker.getPosition().longitude);
	            addPlaceDialog.displayLocationInfo(location);
	            addPlaceDialog.show();
			}
		});
        
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
				Toast.makeText(EatingActivity.this, 
						"Lat: " + location.getLatitude() + " - Lng: " + location.getLongitude(), 
						Toast.LENGTH_LONG).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
}