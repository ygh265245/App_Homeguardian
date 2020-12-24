package org.techtown.termproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View view = findViewById(R.id.view);
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //againStart에 yes/no로 첫 접속인지 구분
                SharedPreferences userinfo = getSharedPreferences("userinfo", MODE_PRIVATE);
                String againstart = userinfo.getString("againStart", "");

                if (againstart.equals("yes")) {
                    //화면 터치 시 한 번 이상 설정을 마쳤을때 (바로 list 페이지로 넘어감)
                    Intent intent = new Intent(getApplicationContext(), activity_todolist.class);
                    startActivity(intent);
                    return true;
                }
                else {
                   //화면 터치 시 앱 설치 후 첫 접속(설정x, 이름설정 페이지로 넘어감)

                    Intent intent = new Intent(getApplicationContext(), activity_name.class);
                    startActivity(intent);
                    return true;
                }
            }
        });

    }

}