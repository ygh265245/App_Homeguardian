package org.techtown.termproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class activity_fagment extends AppCompatActivity { //fragment가려고 만듦
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment);
    }
}