package com.example.luis.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;



//add an action bar to the activity
public class MainActivity extends ActionBarActivity {

    //onCreate() creates a new  Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sets the activity content with a layout that defines the UI
        setContentView(R.layout.activity_main);

        //get a reference to the actionBar object by calling getSupportActionBar()
        getSupportActionBar().setDisplayShowHomeEnabled(true); //include the app logo
        getSupportActionBar().setLogo(R.drawable.ic_launcher);//set the logo
        getSupportActionBar().setDisplayUseLogoEnabled(true);//display app logo

            // add fragment to activity
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())//add the fragment to the container
                    .commit();//complete the changes added above
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                // intent.putExtra(EXTRA_MESSAGE,forecast);
                startActivity(intent);
                return true;

            case R.id.action_map_location:
                showLocationOnMap();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLocationOnMap()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).build();
        Intent mapIntent = new Intent();
        mapIntent.setAction(Intent.ACTION_VIEW);
        mapIntent.putExtra("location", location);
        mapIntent.setData(geoLocation);
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
    }

    }

}
