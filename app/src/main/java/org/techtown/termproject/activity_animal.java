package org.techtown.termproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class activity_animal extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animal);
        Button petyes=(Button)findViewById(R.id.button2);
        Button petno=(Button)findViewById(R.id.button3);

        //yes 클릭 시 sharedpreference의 userpet에 yes(반려동물 있음) 저장
        //저장 후 알림시간 설정페이지로 넘어감
        petyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences userinfo= getSharedPreferences("userinfo", MODE_PRIVATE);
                SharedPreferences.Editor editor= userinfo.edit();
                editor.putString("userpet","Yes");
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), activity_time.class);
                startActivity(intent);
            }
        });

        //no 클릭 시 sharedpreference의 userpet에 no(반려동물 없음) 저장
        //저장 후 알림 시간 설정페이지로 넘어감
        petno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences userinfo= getSharedPreferences("userinfo", MODE_PRIVATE);
                SharedPreferences.Editor editor= userinfo.edit();
                editor.putString("userpet","No");
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), activity_time.class);
                startActivity(intent);
            }
        });
    }
}