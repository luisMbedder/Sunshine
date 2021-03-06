package com.example.luis.sunshine.app;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * forecast fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    ArrayAdapter<String> mWeatherDataAdapter;
    public final static String EXTRA_MESSAGE = "com.example.luis.sunshine.app.MESSAGE";

    public ForecastFragment() {

int a=0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this fragment has menu items to contribute(Refresh button)
        setHasOptionsMenu(true);
    }

    @Override
    //creates and returns the view heirarchy associated with the fragment
    //inflater: LayoutInflater object that can be used to inflate any views in the fragment
    // ViewGroup:if non-null,this is the parent view that the fragment's UI should be attached to.
    // The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
    //SavedInstanceState:If non-null, this fragment is being re-constructed from a previous saved state as given here.
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mWeatherDataAdapter = new ArrayAdapter<String>
                (
                //The current context(this fragments parent activity)
                getActivity(),
                //ID of list item layout
                R.layout.list_item_forecast,
                //ID of the textview so arrayadpater knows how to instantiate a TextView for each row
                R.id.list_item_forecast_textview,
                //weather data
                new ArrayList<String>()
                );
                //create listview and use textview objects for each row to populate the list.
                // each weather day is rendered in a textview. So each row in the listview(defined in
                // fragment_main.xml) is a textview defined by list_item_forecast.xml
                ListView myListView = (ListView) rootView.findViewById(R.id.list_view_forecast);
                myListView.setAdapter(mWeatherDataAdapter);
                //Register a callback to be invoked when an item in this AdapterView has been clicked
                myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    //  Callback method to be invoked when an item in this AdapterView has been clicked.
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //get activity the fragment is associated with
                        Context context = getActivity();
                        //parent:The adapterview where the click happened
                        Object textObject = parent.getItemAtPosition(position);
                        String forecast = textObject.toString();
                        //starting a new activity is packaged as an intent
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, forecast);
                        //start detail Activity
                        startActivity(intent);
                    }
                });
//  task.execute();


    return rootView;
}


    @Override
    //this is called by setting setHasOptionsMenu(true)
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateWeather();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateWeather()
    {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //get location value, if no key is found use the default location
        String location = sharedPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        weatherTask.execute(location);
    }




    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }



    public class FetchWeatherTask extends AsyncTask<String,Void,String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        String[] resultArray;

        /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double highTemp, double lowTemp) {

            // metric or imperial units
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String units = sharedPref.getString(getString(R.string.pref_temperature_units_key), getString(R.string.pref_units_metric));
            if(units.equals(getString(R.string.pref_units_imperial)))
            {
                //convert from celcius to farenheit
                highTemp = (highTemp*1.8)+32;
                lowTemp = (lowTemp*1.8)+32;
            }
            else if(!units.equals(getString(R.string.pref_units_metric)))
            {
                Log.d(LOG_TAG,"Unit type not found:"+units);
            }
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(highTemp);
            long roundedLow = Math.round(lowTemp);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy: constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)throws JSONException {

            String LOG_TAG = " getWeatherDataFromJson";
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < resultStrs.length; i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long. We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp". Try not to name variables
                // "temp" when working with temperature. It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;

                String str =  resultStrs[i];
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }


        @Override
        protected String[] doInBackground(String... params) {// These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            if(params.length==0){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APP_ID = "appid";

                Uri builtUri=Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,"json")
                        .appendQueryParameter(UNITS_PARAM,"metric")
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(7))
                        .appendQueryParameter(APP_ID, "69cbb710b1fb7c55db947225d8e802ad").build();
                URL url = new URL(builtUri.toString());


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG,"Forecast JSON String: " + forecastJsonStr);

               // WeatherDataParser parsedStr = new WeatherDataParser();
                   // parsedStr.
                  //  parsedStr.getMaxTemperatureForDay(forecastJsonStr,0);

            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            }
            catch(Exception ex ){
                Log.e("ForecastFragment", "JSON Error ", ex);
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                resultArray = getWeatherDataFromJson(forecastJsonStr, 7);
                return resultArray;

            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        return null;

        }

        //this method runs on the UI thread, its invoked when doInBackground() finishes.The result
        //of doInBackground() is passed to this method as a parameter.
        @Override
        protected void onPostExecute(String[] result) {
            if(result!=null) {
                mWeatherDataAdapter.clear();
                for (String forecastStr : result) {
                    mWeatherDataAdapter.add(forecastStr);
                }
            }


        }



    }

}