package com.example.weather1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {


//    initiating the view
    TextView cityText;
    Button searchButton;

    TextView resultTemperature;
    TextView resultSummary;
    TextView resultTimeZone;

    private static  final int REQUEST_LOCATION=1;

    LocationManager locationManager;
    String latitude,longitude;


// Creating the Weather class for the function of the calling
    class Weather extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... address) {
            try {
                URL url = new URL(address[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                int data = isr.read();
                String content = "";
                char ch;
                while (data != -1) {
                    ch = (char) data;
                    content = content + ch;
                    data = isr.read();
                }
                return content;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

//    Search vwe where the cities can be searched
    public void search(View view) {

        resultTemperature = (TextView) findViewById(R.id.resultTemperatureTextView);
        resultSummary = (TextView) findViewById(R.id.resultSummaryTextView);
        resultTimeZone = (TextView) findViewById(R.id.resultTimeZoneTextView);

        String cName = cityText.getText().toString();
        String content;
        Weather weather = new Weather();
        try {

//            calling the API
            content = weather.execute("https://openweathermap.org/data/2.5/weather?q=" + cName + "&appid=b6907d289e10d714a6e88b30761fae22").get();
            Log.i("content", content);
//
//            getting the informatkion through JSOn object
            JSONObject jsonObject = new JSONObject(content);
            String weatherData = jsonObject.getString("weather");
            String mData = jsonObject.getString("main");
            String tData = jsonObject.getString("name");
//            Log.i("weatherData", weatherData);

            JSONArray jsonArray = new JSONArray(weatherData);
//

            String main = "";
            String summary = "";
            String temp = "";

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject weatherPart = jsonArray.getJSONObject(i);
                main = weatherPart.getString("main");
                summary = weatherPart.getString("description");
            }

            JSONObject jsonM = new JSONObject(mData);
            temp = jsonM.getString("temp");


            int tempC = Integer.parseInt(temp);
            int tempF = (tempC*9/5)+32;

//             Displaying the information
            resultTemperature.setText(String.valueOf(tempC)+"°C/"+new DecimalFormat("##.##").format(tempF)+"°F");
            resultSummary.setText("Summary :\n"+summary);
            resultTimeZone.setText("TimeZone : "+cName);

            Log.i("sum",summary);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityText = (EditText) findViewById(R.id.cityText);
        searchButton = (Button) findViewById(R.id.searchButton);

        resultTemperature = (TextView) findViewById(R.id.resultTemperatureTextView);
        resultSummary = (TextView) findViewById(R.id.resultSummaryTextView);
        resultTimeZone = (TextView) findViewById(R.id.resultTimeZoneTextView);

        String content;

//        Code for getting the co-ordinates of the current location
        //Add permission

        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Check gps is enable or not

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            //Write Function To enable gps

            OnGPS();
        }
        else
        {
            //GPS is already On then

            getLocation();
        }

        Weather weather = new Weather();
        try {

//            getting the weather information of the current location
            content = weather.execute("https://api.darksky.net/forecast/49c2dc7e2cc978678e9ec836e6d92bed/"+latitude+","+longitude).get();
            Log.i("content", content);

            JSONObject jsonObject = new JSONObject(content);
            String currentWeatherData = jsonObject.getString("currently");
            Log.i("current",currentWeatherData);


            String summary = "";
            String temperature = "";
            String timeZone = "";

            JSONObject jsonM = new JSONObject(currentWeatherData);
            summary = jsonM.getString("summary");
            temperature = jsonM.getString("temperature");
            timeZone = jsonObject.getString("timezone");

            double tempF = Double.parseDouble(temperature);
            double tempC = (tempF-32)*5/9;

            resultTemperature.setText(new DecimalFormat("##.##").format(tempC)+"°C/"+String.valueOf(tempF)+"°F");
            resultSummary.setText("Summary :\n"+summary);
            resultTimeZone.setText("TimeZone : "+timeZone);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLocation() {

        //Check Permissions again

        if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,

                Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else
        {
            Location LocationGps= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps !=null)
            {
                double lat=LocationGps.getLatitude();
                double longi=LocationGps.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);

            }
            else if (LocationNetwork !=null)
            {
                double lat=LocationNetwork.getLatitude();
                double longi=LocationNetwork.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);
            }
            else if (LocationPassive !=null)
            {
                double lat=LocationPassive.getLatitude();
                double longi=LocationPassive.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);

            }
            else
            {
                Toast.makeText(this, "Can't Get Your Location", Toast.LENGTH_SHORT).show();
            }

            //Thats All Run Your App
        }

    }

    private void OnGPS() {

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

}

