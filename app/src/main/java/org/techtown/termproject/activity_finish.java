package org.techtown.termproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.termproject.R;

public class activity_finish extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finish);
        View view = findViewById(R.id.view2);

        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                    Intent intent = new Intent(getApplicationContext(), activity_todolist.class);
                    startActivity(intent);
                    return true;

            }
        });
    }
}