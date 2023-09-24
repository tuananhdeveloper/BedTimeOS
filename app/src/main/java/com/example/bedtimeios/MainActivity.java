package com.example.bedtimeios;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.view.ViewGroup;

import com.example.bedtimeios.custom.BedTimeSeekBar;
import com.example.bedtimeios.utils.Converter;

import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        TextView bedTimeText = findViewById(R.id.text_bedtime_value);
        BedTimeSeekBar bedTimeSeekBar = findViewById(R.id.mprogress_bar);
        bedTimeSeekBar.setOnTimeChangeListener((bedTimeMinites, sleepTimeMinutes) -> {
            bedTimeText.setText(Converter.minutesToHHmm(bedTimeMinites));
            v.vibrate(20);
        });
    }
}