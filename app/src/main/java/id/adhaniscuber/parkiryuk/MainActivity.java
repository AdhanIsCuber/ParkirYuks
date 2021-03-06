package id.adhaniscuber.parkiryuk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.com.bloder.magic.view.MagicButton;
import id.adhaniscuber.parkiryuk.model.ParkirData;
import id.adhaniscuber.parkiryuk.multidex.AppController.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, LocationListener {

    private Toolbar toolbar;
    public GoogleMap mMap;
    private ProgressDialog loading;
    private static  String TAG = MainActivity.class.getSimpleName();
    private String sNama, sAlamat, sKota, sJenis, sBiayaMotor, sBiayaMobil, sBiayaMotorTambah, sBiayaMobilTambah, sMaxBiayaMotor, sMaxBiayaMobil, sKeterangan, sMotor, sMobil, sTotalKendaraan;
    private Double flat, flong;
    private ArrayList<ParkirData> parkirData;

    private Location mLastLocation;
    private Marker mCurrLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MagicButton magicButton = (MagicButton) findViewById(R.id.magic_button);
        magicButton.setMagicButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sulap = new Intent(MainActivity.this, DaftarparkirActivity.class);
                startActivity(sulap);
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sendRequestArray();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent i = new Intent(MainActivity.this, AboutActivity.class);
            this.startActivity(i);
            return super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),15));
        marker.showInfoWindow();

        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void sendRequestArray() {

        String url = "http://parkiryuk.pe.hu/api.php";
        loading = ProgressDialog.show(this, "Mohon tunggu...", "Mengambil data...", false, false);

        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        try {
                            parkirData = new ArrayList<ParkirData>();
                            for (int park = 0; park < response.length(); park++){
                                JSONObject lokasi = (JSONObject) response.get(park);

                                sNama = lokasi.getString("nama");
                                sAlamat = lokasi.getString("alamat");
                                sKota = lokasi.getString("kota");
                                sJenis = lokasi.getString("jenis");
                                sBiayaMotor = lokasi.getString("biaya_motor");
                                sBiayaMobil = lokasi.getString("biaya_mobil");
                                sBiayaMotorTambah = lokasi.getString("biaya_motor_tambah");
                                sBiayaMobilTambah = lokasi.getString("biaya_mobil_tambah");
                                sMaxBiayaMotor = lokasi.getString("max_biaya_motor");
                                sMaxBiayaMobil = lokasi.getString("max_biaya_mobil");
                                sKeterangan = lokasi.getString("keterangan");
                                sMotor = lokasi.getString("motor");
                                sMobil = lokasi.getString("mobil");
                                sTotalKendaraan = lokasi.getString("total_kendaraan");
                                String sLat = lokasi.getString("lat");
                                String sLong = lokasi.getString("long");

                                //Toast.makeText(MainActivity.this, "" + sNama + sLat + sLong, Toast.LENGTH_SHORT).show();

                                flat = Double.parseDouble(sLat);
                                flong = Double.parseDouble(sLong);
                                LatLng fixlok = new LatLng(flat, flong);

                                mMap.addMarker(new MarkerOptions()
                                        .title(sNama)
                                        .position(fixlok)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_markerpy)));

                                parkirData.add(new ParkirData(sNama, sAlamat,sKota, sJenis, sBiayaMotor, sBiayaMobil, sBiayaMotorTambah, sBiayaMobilTambah, sMaxBiayaMotor, sMaxBiayaMobil, sKeterangan, sMotor, sMobil, sTotalKendaraan, flat,flong));

                                //Toast.makeText(MainActivity.this, "" + fixlok, Toast.LENGTH_SHORT).show();
                            }
                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker marker) {
                                    int posisi = Integer.parseInt(marker.getId().replace("m",""));
                                    Intent detail = new Intent(MainActivity.this, DetailActivity.class);
                                    detail.putExtra("nama", parkirData.get(posisi).getNama());
                                    detail.putExtra("alamat", parkirData.get(posisi).getAlamat());
                                    detail.putExtra("kota", parkirData.get(posisi).getKota());
                                    detail.putExtra("jenis", parkirData.get(posisi).getJenis());
                                    detail.putExtra("biaya_motor", parkirData.get(posisi).getBiayaMotor());
                                    detail.putExtra("biaya_mobil", parkirData.get(posisi).getBiayaMobil());
                                    detail.putExtra("biaya_motor_tambah", parkirData.get(posisi).getBiayaMotorTambah());
                                    detail.putExtra("biaya_mobil_tambah", parkirData.get(posisi).getBiayaMobilTambah());
                                    detail.putExtra("max_biaya_motor", parkirData.get(posisi).getMaxBiayaMotor());
                                    detail.putExtra("max_biaya_mobil", parkirData.get(posisi).getMaxBiayaMobil());
                                    detail.putExtra("keterangan", parkirData.get(posisi).getKeterangan());
                                    detail.putExtra("motor", parkirData.get(posisi).getMotor());
                                    detail.putExtra("mobil", parkirData.get(posisi).getMobil());
                                    detail.putExtra("total_kendaraan", parkirData.get(posisi).getTotalKendaraan());

                                    detail.putExtra("long", parkirData.get(posisi).getPylongitude());
                                    detail.putExtra("lat", parkirData.get(posisi).getPylatitude());
                                    startActivity(detail);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Errors : " + e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                        loading.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error : " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                loading.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(req);
    }




}
