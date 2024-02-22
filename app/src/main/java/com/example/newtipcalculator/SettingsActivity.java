package com.example.newtipcalculator;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {


    /*
    This code adds the fragment to an activity using the getFragmentManager method to get a
    FragmentManager object

    The beginTransaction, replace and commit methods are called.

    android.R.id.content is replaced with the SettingsFragment content
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Display the fragment as the main content
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}
