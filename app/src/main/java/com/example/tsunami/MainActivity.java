package com.example.tsunami;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private static final String USGS_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2012-01-01&endtime=2012-12-01&minmagnitude=6";
    Event earthquake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TsunamiAsync tsunamiAsync = new TsunamiAsync();
        tsunamiAsync.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class TsunamiAsync extends AsyncTask<URL, Void, Event> {


        @Override
        protected Event doInBackground(URL... urls) {
            URL url1 = null;
            try {
                url1 = new URL(USGS_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                earthquake = extractDataFromJSON(jsonResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return earthquake;
        }

        @Override
        protected void onPostExecute(Event event) {
            updateUI(earthquake);
        }

        private String makeHttpRequest(URL url) throws IOException {
            //URL url1 = new URL(USGS_URL);
            HttpURLConnection con = null;
            String jsonResponse = "";
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                jsonResponse = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            return jsonResponse;
        }

        private Event extractDataFromJSON(String jsonResponse) throws JSONException {
            JSONObject baseJSONResponse = new JSONObject(jsonResponse);
            JSONArray featureArray = baseJSONResponse.getJSONArray("features");

            try {
                if (featureArray.length() > 0) {
                    JSONObject firstFeature = featureArray.getJSONObject(0);
                    JSONObject properties = firstFeature.getJSONObject("properties");

                    String title = properties.getString("title");
                    long date = properties.getLong("date");
                    int tsunamiAlert = properties.getInt("tsunami");

                    return new Event(title, getDatelong(date), tsunamiAlert);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private long getDatelong(long milliseconds) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy\nhh:mm:ss.SSS");
            return Long.parseLong(formatter.format(milliseconds));
        }
    }

    private void updateUI(Event earthquake) {

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(earthquake.title);

        TextView dateTextView = (TextView) findViewById(R.id.date);
        dateTextView.setText((int) earthquake.date);

        TextView tsunamiTextView = (TextView) findViewById(R.id.tsunamiAlert);
        titleTextView.setText(earthquake.EventstunamiAlert);

    }
}