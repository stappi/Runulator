package com.stappert.runulator.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.stappert.runulator.R;
import com.stappert.runulator.dialogs.DistanceDialog;
import com.stappert.runulator.dialogs.LoadRunDialog;
import com.stappert.runulator.utils.SettingsManager;

import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MainActivityTabAdapter mainActivityTabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivityTabAdapter = new MainActivityTabAdapter(getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(mainActivityTabAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    /**
     * Creates main menu.
     *
     * @param menu menu
     * @return true, if menu is successfully created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Called, when menu item of main menu is selected.
     *
     * @param item selected menu item
     * @return true, if process was successfully
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_load_run:
                new LoadRunDialog(mainActivityTabAdapter.getTabRun())
                        .show(this.getSupportFragmentManager(), getString(R.string.load_run));
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_info:
                startActivity(new Intent(this, InfoActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called on resume of activity.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
}