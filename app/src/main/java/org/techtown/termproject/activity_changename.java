package org.techtown.termproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.termproject.R;

//setting에서 선택가능한 name 변경 창. activity_name.java와 동일하다.
public class activity_changename extends AppCompatActivity {
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changename);
        SharedPreferences userinfo = getSharedPreferences("userinfo", MODE_PRIVATE);
        userinfo.getString("name", null);

        editText=findViewById(R.id.editTextTextPersonName);

        Button bt=(Button)findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences userinfo= getSharedPreferences("userinfo", MODE_PRIVATE);
                SharedPreferences.Editor editor= userinfo.edit();
                editor.putString("username",editText.getText().toString());
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), activity_todolist.class);
                startActivity(intent);
            }
        });
    }


}