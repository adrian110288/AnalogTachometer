package com.adrianlesniak.analogtachometer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.SeekBar;


public class MainActivity extends ActionBarActivity {

    private Tachometer mSpeedometer;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpeedometer = (Tachometer) findViewById(R.id.speedometer);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        mSeekBar.setMax(100);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                float progressF = (float)progress/100;

                mSpeedometer.setSpeed(progressF);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
