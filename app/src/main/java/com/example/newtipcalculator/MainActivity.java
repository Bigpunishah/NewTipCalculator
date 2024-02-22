package com.example.newtipcalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener, View.OnKeyListener, SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

    private EditText billAmountEditText;
    private TextView percentTextView;
    private SeekBar percentSeekBar;
    private TextView tipTextView;
    private TextView totalTextView;
    private RadioGroup roundingRadioGroup;
    private RadioButton roundNoneRadioButton;
    private RadioButton roundTipRadioButton;
    private RadioButton roundTotalRadioButton;
    private Spinner splitSpinner;
    private TextView perPersonLabel;
    private TextView perPersonTextView;

    //Shared reference to save our values
    private SharedPreferences savedValues;

    //Defining values
    private final int ROUND_NONE = 0;
    private final int ROUND_TIP = 1;
    private final int ROUND_TOTAL = 2;

    private String billAmountString = "";
    private float tipPercent = .15f;
    private int rounding = ROUND_NONE;
    private int split = 1;

    //Chapter 8 preference variables
    //Get preferences step 1
    private SharedPreferences prefs;
    private boolean rememberTipPercent = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Now going to find those values from their ID.
        billAmountEditText = (EditText) findViewById(R.id.billAmountEditText);
        percentTextView = (TextView) findViewById(R.id.percentTextView);
        percentSeekBar = (SeekBar) findViewById(R.id.percentSeekBar);
        tipTextView = (TextView) findViewById(R.id.tipTextView);
        totalTextView = (TextView) findViewById(R.id.totalTextView);
        roundingRadioGroup = (RadioGroup) findViewById(R.id.roundingRadioGroup);
        roundNoneRadioButton = (RadioButton) findViewById(R.id.roundNoneRadioButton);
        roundTipRadioButton = (RadioButton) findViewById(R.id.roundTipRadioButton);
        roundTotalRadioButton = (RadioButton) findViewById(R.id.roundTotalRadioButton);
        splitSpinner = (Spinner) findViewById(R.id.splitSpinner);
        perPersonLabel = (TextView) findViewById(R.id.perPersonLabel);
        perPersonTextView = (TextView) findViewById(R.id.perPersonTextView);



        //2 set array adapter for the Spinner object
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.split_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        splitSpinner.setAdapter(adapter);

        billAmountEditText.setOnEditorActionListener(this);
        billAmountEditText.setOnKeyListener(this);

        percentSeekBar.setOnSeekBarChangeListener(this);
        percentSeekBar.setOnKeyListener(this);

        roundingRadioGroup.setOnCheckedChangeListener(this);
        roundingRadioGroup.setOnKeyListener(this);

        splitSpinner.setOnItemSelectedListener(this);

        savedValues = getSharedPreferences("savedValues", MODE_PRIVATE);

        //Get preferences step 2
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //Get preferences step 3
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

    }//End onCreate

    public void onResume() {
        //Get preferences
        super.onResume();
            rememberTipPercent = prefs.getBoolean("pref_remember_percent", true);
            //rounding = Integer.parseInt(prefs.getString("pref_rounding", "0"));

        try {
            rounding = Integer.parseInt(prefs.getString("pref_rounding", "0"));
        } catch (NumberFormatException e) {
            String error = e.toString();
            // Handle the exception, e.g., by using a default value or logging an error
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // or Log.e(TAG, "Error parsing rounding preference", e);
            rounding = 0; // Set a default value or handle it in another way
        }


            //Use references
            //Get the instance variables
            billAmountString = prefs.getString("billAmountString", "");
            billAmountEditText.setText(billAmountString);
            //If remember tip checkbox is checked get it from preference
            if(rememberTipPercent){
                tipPercent = prefs.getFloat("tipPercent", 0.15f);
            } else {
                tipPercent = 0.15f;
            }

            //Update the progress bar
            int progress = Math.round(tipPercent * 100);
            percentSeekBar.setProgress(progress);

            //Update the radio buttons
            if(rounding == ROUND_NONE){
                roundNoneRadioButton.setChecked(true);
            } else if(rounding == ROUND_TIP){
                roundTipRadioButton.setChecked(true);
            } else if (rounding == ROUND_TOTAL){
                roundTotalRadioButton.setChecked(true);
        } //end if

        calculateAndDisplay();
    }//end onResume

    @Override
    public void onPause(){ //need to import shared preferences editor
        //save the instance variable
        super.onPause(); //calls the superclass to complete the creation of the activity

        SharedPreferences.Editor editor = prefs.edit();
        editor.apply(); // new?
    } //End onPause

    public void calculateAndDisplay(){
        //get bill amount
        billAmountString = billAmountEditText.getText().toString();
        float billAmount;
        if(billAmountString.equals("")){
            billAmount = 0;
        } else {
            billAmount = Float.parseFloat(billAmountString);
        }

        //get tip percent
        int progress = percentSeekBar.getProgress();
        tipPercent = (float) progress / 100;

        //calculate tip & total
        float tipAmount = 0;
        float totalAmount = 0;
        float tipPercentToDisplay = 0;
        if(rounding == ROUND_NONE){
            tipAmount = billAmount * tipPercent;
            totalAmount = billAmount + tipAmount;
            tipPercentToDisplay = tipPercent;

        } else if(rounding == ROUND_TIP){
            tipAmount = StrictMath.round(billAmount * tipPercent);
            totalAmount = billAmount + tipAmount;
            tipPercentToDisplay = tipAmount/billAmount;

        } else if(rounding == ROUND_TOTAL){
            float tipNotRounded = billAmount * tipPercent;
            totalAmount = StrictMath.round(billAmount + tipNotRounded);
            tipAmount = totalAmount - billAmount;
            tipPercentToDisplay = tipAmount/billAmount;
        }

        //calculate split amount & show/hide split amount & show widgets
        float splitAmount = 0;

        if(split == 1){ //no split - hide widgets
            perPersonLabel.setVisibility(View.GONE);
            perPersonTextView.setVisibility(View.GONE);
        } else {
            splitAmount = totalAmount / split;
            perPersonLabel.setVisibility(View.VISIBLE);
            perPersonTextView.setVisibility(View.VISIBLE);
        }

        //Display results with formatting
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipTextView.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmount));
        perPersonTextView.setText(currency.format(splitAmount));

        NumberFormat percent = NumberFormat.getPercentInstance();
        //percentTextView.setText(percent.format(tipPercent));
        percentTextView.setText(percent.format(tipPercentToDisplay));

    }//End calculateAndDisplay



    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
            calculateAndDisplay();
        }
        return false;
    }//end onEditorAction event handler

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        switch(keyCode){
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                calculateAndDisplay();
                //hide soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(billAmountEditText.getWindowToken(), 0);

                //consume  event
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(view.getId() == R.id.percentSeekBar){
                    calculateAndDisplay();
                }
                break;
        }
        //don't consume event
        return false;
    }//end onKey event handler

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        percentTextView.setText(progress + "%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //TO AUTO GENERATED STUB
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        calculateAndDisplay();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch(checkedId){
            case R.id.roundNoneRadioButton:
                rounding = ROUND_NONE;
                break;

            case R.id.roundTipRadioButton:
                rounding = ROUND_TIP;
                break;

            case R.id.roundTotalRadioButton:
                rounding = ROUND_TOTAL;
                break;
        }
        calculateAndDisplay();
    }//end Radio Group event handler

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        split = position + 1;
        calculateAndDisplay();
    } // end spinner onItemSelected

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //Do nothing
    }//end spinner onNothingSelected



    @Override //This makes the menu visible
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tip_calculator_menu, menu);
        return true;
    }//End onCreateOptionsMenu

    @Override //This is when the menu items get selected .. they do ..
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_settings:
                //Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                //Start the settings activity
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;

            case R.id.menu_about:
                //Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
                //Start menu activity
                startActivity(new Intent(getApplicationContext(),AboutActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }//End switch
    }//End onOptionsItemSelected

}//End main activity