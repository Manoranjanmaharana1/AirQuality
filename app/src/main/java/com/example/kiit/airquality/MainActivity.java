package com.example.kiit.airquality;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    boolean Gps = true ;
    String lon,lat;
    String features = "pollutants_concentrations,breezometer_aqi,pollutants_aqi_information";
    int req = 444;
    String url = "https://api.breezometer.com/air-quality/v2/current-conditions";
    String key = "2be7f46329b845c785760aa1dd329da4";
    long min = 30000;
    float dis = 0;
    boolean netAccess;
    Animation anim;

    LocationManager locationManager;
    LocationListener locationListener;

    ImageButton go, gps;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        anim = AnimationUtils.loadAnimation(this,R.anim.show);
        Log.d("Air", "onCreate()");
        if(netAccess = haveNetworkConnection()){
            Snackbar sm = Snackbar.make(findViewById(R.id.parentLayout),"Getting AirQuality of your Location",Snackbar.LENGTH_LONG);
            sm.getView().setBackgroundColor(Color.MAGENTA);
            sm.show();
            getAirQuality();
        }
        else {
            Snackbar sm = Snackbar.make(findViewById(R.id.parentLayout),"Internet Connection Failed!!",Snackbar.LENGTH_LONG);
            sm.getView().setBackgroundColor(Color.RED);
            sm.show();
        }
        gps = findViewById(R.id.imageButton);
        go = findViewById(R.id.imageButton2);
        final EditText editText = findViewById(R.id.editText);
        editText.setCursorVisible(false);
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editText.setCursorVisible(true);
                return false;
            }
        });
        editText.setCursorVisible(true);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Air","searching query");
                try {
                    searchIt();
                    Log.d("Air",Long.toString(SystemClock.elapsedRealtime()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            EditText ed = findViewById(R.id.editText);
            ed.setCursorVisible(false);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final EditText editText = findViewById(R.id.editText);
        editText.setCursorVisible(false);
        Log.d("Air", "onResume()");
        Log.d("Air", "getAirQuality()");
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(netAccess = haveNetworkConnection()){
                    Snackbar sm = Snackbar.make(findViewById(R.id.parentLayout),"Getting AirQuality of your Location",Snackbar.LENGTH_LONG);
                    sm.getView().setBackgroundColor(Color.MAGENTA);
                    sm.show();
                    getAirQuality();
                }
                else {
                    Snackbar sm = Snackbar.make(findViewById(R.id.parentLayout),"Internet Connection Failed!!",Snackbar.LENGTH_LONG);
                    sm.getView().setBackgroundColor(Color.RED);
                    sm.show();
                }
            }
        });
    }
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(netAccess)
        locationManager.removeUpdates(locationListener);
    }

    private void searchIt() throws IOException {
        EditText search =  findViewById(R.id.editText);
        String getSearch = search.getText().toString();
        Log.e("Air",Integer.toString(getSearch.length()));
        Geocoder gc = new Geocoder(this);
        try {
            if(getSearch.length()>0) {
                if(!(netAccess = haveNetworkConnection())) {
                    Snackbar sm = Snackbar.make(findViewById(R.id.parentLayout), "Internet Connection Failed!!", Snackbar.LENGTH_LONG);
                    sm.getView().setBackgroundColor(Color.RED);
                    sm.show();
                }
                else{
                Gps = false;
                List<Address> address = gc.getFromLocationName(getSearch, 1);
                    if (address.isEmpty() == false) {
                    Address add = address.get(0);
                    double l1 = add.getLatitude();
                    double l2 = add.getLongitude();
                    getAllData(Double.toString(l1), Double.toString(l2));
                    locationManager.removeUpdates(locationListener);
                    }
                    else {
                        TextView t1,t2,t3,t4,t5,t6;
                        ImageView im = findViewById(R.id.showstatus);
                        t1 = findViewById(R.id.textView);
                        t2 = findViewById(R.id.textView2);
                        t3 = findViewById(R.id.o3);
                        t4 = findViewById(R.id.co);
                        t5 = findViewById(R.id.so2);
                        t6 = findViewById(R.id.no2);
                        t1.setText("No Such Location Found :(");
                        t2.setText("!!!");
                        t3.setText("O3\n?");
                        t4.setText("CO\n?");
                        t5.setText("SO2\n?");
                        t6.setText("NO2\n?");
                        im.setImageResource(R.drawable.air);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void getAirQuality() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Gps = true;
        EditText editText = findViewById(R.id.editText);
        editText.setText(null);
        locationListener = new LocationListener() {



            @Override
            public void onLocationChanged(Location location) {


                Log.d("Air", "onLocationChanged()");
                lon = String.valueOf(location.getLongitude());
                lat = String.valueOf(location.getLatitude());
               // Toast.makeText(MainActivity.this,"Location",Toast.LENGTH_LONG).show();
                Log.d("Air",lon+" "+lat);
                Log.d("Air","getAllData()");
                if(!(netAccess = haveNetworkConnection())) {
                    Snackbar sm = Snackbar.make(findViewById(R.id.parentLayout), "Internet Connection Failed!!", Snackbar.LENGTH_LONG);
                    sm.getView().setBackgroundColor(Color.RED);
                    sm.show();
                }
                else if(Gps) getAllData(lat,lon);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

                //Log.d("Air", "Enabled Location");
            }

            @Override
            public void onProviderDisabled(String provider) {


               // Log.d("Air", "Disabled Location");
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},req);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, min, dis, locationListener);
        Log.d("Air","requestLocation");
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, min, dis, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == req){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Air","Permission granted");
                getAirQuality();
            }
            else {
                Log.d("Air","Not granted");
            }
        }
    }
    private void getAllData(String lat,String lon){
        String new_add = null;
        int i;
        Geocoder gc = new Geocoder(this,Locale.getDefault());
        try {
            List<Address> addresses = gc.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lon),1);
            if(addresses.isEmpty() == false || addresses.size()!=0){
                Address add = addresses.get(0);
                String address = add.getAddressLine(0);
                for( i=0;address.charAt(i)!=',';i++);
                new_add = address.substring(i+1,address.length());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("lat",lat);
        urlBuilder.addQueryParameter("lon", lon);
        urlBuilder.addQueryParameter("features",features);
        urlBuilder.addQueryParameter("key",key);
        String URL = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(URL)
                .build();
        final String finalAddress = new_add;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responded = "";
                Log.d("Air",responded = response.body().string());

                final String finalResponded = responded;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUi(finalResponded, finalAddress);
                        //t.getBackground().setAlpha(45);
                    }
                });

            }
        });
    }
    private void setPic(String value){
        int aqi = Integer.parseInt(value);
        ImageView img = findViewById(R.id.showstatus);
        if(aqi>=0 && aqi <16)
            img.setImageResource(R.drawable.air6);
        else if(aqi>=16 && aqi< 34)
            img.setImageResource(R.drawable.air5);
        else if(aqi>=34 && aqi< 50)
            img.setImageResource(R.drawable.air4);
        else if(aqi>=50 && aqi<68)
            img.setImageResource(R.drawable.air3);
        else if(aqi>=68 && aqi<84)
            img.setImageResource(R.drawable.air2);
        else img.setImageResource(R.drawable.air1);
        img.startAnimation(anim);
    }
    private void updateUi(String responded, final String finalAddress){
        String aqi = " ",color = " ";
        boolean dataAvailable = false;
        try {
            JSONObject obj1 = new JSONObject(responded);
            JSONObject obj2 = obj1.getJSONObject("data");
            dataAvailable = obj2.getBoolean("data_available");
            if(dataAvailable) {
                JSONObject obj3 = obj2.getJSONObject("indexes");
                JSONObject obj4 = obj3.getJSONObject("baqi");
                JSONObject obj5 = obj2.getJSONObject("pollutants");
                JSONObject obj6 = obj5.getJSONObject("co");
                JSONObject obj61 = obj6.getJSONObject("concentration");
                TextView co = findViewById(R.id.co);
                co.setText("CO\n" + obj61.getString("value") + "\n" + obj61.getString("units"));
                JSONObject obj7 = obj5.getJSONObject("no2");
                JSONObject obj71 = obj7.getJSONObject("concentration");
                TextView no2 = findViewById(R.id.no2);
                no2.setText("NO2\n" + obj71.getString("value") + "\n" + obj71.getString("units"));
                JSONObject obj8 = obj5.getJSONObject("o3");
                JSONObject obj81 = obj8.getJSONObject("concentration");
                TextView o3 = findViewById(R.id.o3);
                o3.setText("O3\n" + obj81.getString("value") + "\n" + obj81.getString("units"));
                JSONObject obj9 = obj5.getJSONObject("so2");
                JSONObject obj91 = obj9.getJSONObject("concentration");
                TextView so2 = findViewById(R.id.so2);
                so2.setText("SO2\n" + obj91.getString("value") + "\n" + obj91.getString("units"));

                aqi = obj4.getString("aqi_display");
                color = obj4.getString("color");
            }
            else{
                TextView t1,t2,t3,t4,t5,t6;
                ImageView im = findViewById(R.id.showstatus);
                t1 = findViewById(R.id.textView);
                t2 = findViewById(R.id.textView2);
                t3 = findViewById(R.id.o3);
                t4 = findViewById(R.id.co);
                t5 = findViewById(R.id.so2);
                t6 = findViewById(R.id.no2);
                t1.setText("No Such Location Found :(");
                t2.setText("!!!");
                t3.setText("O3\n?");
                t4.setText("CO\n?");
                t5.setText("SO2\n?");
                t6.setText("NO2\n?");
                im.setImageResource(R.drawable.air);
            }
            Log.d("Air",aqi);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        final String finalResponded = aqi;
        final String finalColor = color;
        TextView t = findViewById(R.id.textView);
        TextView t2 = findViewById(R.id.textView2);
        t2.setTextColor((Color.parseColor(finalColor)));
        t2.setText(finalResponded);
        t.setText(finalAddress);
        setPic(finalResponded);
    }
}
