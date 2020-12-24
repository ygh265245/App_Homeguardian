package org.techtown.termproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static android.widget.Toast.LENGTH_SHORT;

public class activity_name extends AppCompatActivity {
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name);
        editText=findViewById(R.id.editTextTextPersonName);
        Button bt=(Button)findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Sharedpreference에 name 저장
                SharedPreferences userinfo= getSharedPreferences("userinfo", MODE_PRIVATE);
                SharedPreferences.Editor editor= userinfo.edit();
                editor.putString("username",editText.getText().toString());
                editor.commit();

                //펫 여부 설정 페이지(animal)로 넘어감.
                Intent intent = new Intent(getApplicationContext(), activity_animal.class);
                startActivity(intent);
            }
        });
    }


}
