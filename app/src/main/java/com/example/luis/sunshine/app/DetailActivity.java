package com.example.luis.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuInflater;
//import android.widget.ShareActionProvider;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            // add DetailFragment to Detail activity
            getSupportFragmentManager().beginTransaction().add(R.id.container, new DetailFragment()).commit();

        }
    }

    @Override
    //this method is onyl called the first time the options menu is displayed
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private  ShareActionProvider mShareActionProvider;

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private String weatherDetails;

        public DetailFragment()
        {
            //set true to call onCreateOptionsMenu
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
        {
            //Inflate menu resource file
            inflater.inflate(R.menu.detailfragment, menu);

            //Locate MenuItem with ShareActionProvider
            MenuItem item = menu.findItem(R.id.menu_item_share);
            //Get the provider and hold onto it to set/change the share intent.
            //The action initially appears as a button or menu item, but when the user clicks the action,
            // the action provider controls the action's behavior in any way you want to define.
            // For example, the action provider might respond to a click by displaying a menu.
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            //Attach an intent to this ShareActionProvider. You can update this at an time,
            //like when the user selects a new piece of data they might like to share.
            if(mShareActionProvider!=null)
            {
                mShareActionProvider.setShareIntent(createShareMenuIntent());
            }
            else
            {
                Log.d(LOG_TAG, "Share Action Provider is null!");
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
        {
            //container:DetailActivity container(frameLayout)
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            //getIntent() returns the intent that started this activity(forecastFragment)
            Intent intent = getActivity().getIntent();
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if(intent!=null && intent.hasExtra(ForecastFragment.EXTRA_MESSAGE))
            {
                //pull forecast string from intent
                weatherDetails = intent.getStringExtra(ForecastFragment.EXTRA_MESSAGE);
                ((TextView) rootView.findViewById(R.id.detail_text)).setText(weatherDetails);
            }
            return rootView;
        }

        private Intent createShareMenuIntent()
        {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.putExtra(Intent.EXTRA_TEXT, weatherDetails + FORECAST_SHARE_HASHTAG);
            shareIntent.setType("text/plain");
            return shareIntent;
        }
    }

}


