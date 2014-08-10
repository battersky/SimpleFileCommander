package com.batter.simplefilecommander.ui;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import com.commander.file.batter.filecommander.R;

import java.util.ArrayList;
import java.util.List;

import utils.StorageManager;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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
        if (id == R.id.action_settings) {
            PlaceholderFragment fragment =
                    (PlaceholderFragment)getSupportFragmentManager().findFragmentById(R.id.container);
            fragment.setText();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private TextView mTextView;

        public PlaceholderFragment() {
        }

        public void setText() {
            if (mTextView != null) {
                String text = "The main storage is " + StorageManager.
                        getDisplayedAvailableExternalStorage(this.getActivity());

                mTextView.setText(text);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mTextView = (TextView)this.getView().findViewById(R.id.content_text);
            if (mTextView != null) {
                String text = "The main storage is " + StorageManager.
                        getDisplayedAvailableExternalStorage(this.getActivity());

                mTextView.setText(text);
            }

            List<StorageManager.StorageInfo> list = StorageManager.getStorageList();
            for (int i = 0; i < list.size(); i++) {
                StorageManager.StorageInfo info = list.get(i);
                Log.d("Batter", info.getDisplayName());
            }
        }
    }
}
