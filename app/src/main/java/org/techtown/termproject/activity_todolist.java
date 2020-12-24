package org.techtown.termproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class activity_todolist extends AppCompatActivity {

    activity_weather fragment;
    TextView nametext;
    TextView dateNow;
    TextView countText;
    private static final String SETTINGS_PLAYER = "settings_player";
    private static final String SETTINGS_PLAYER_JSON="settings_item_json";
    ArrayList<String> items;
    ArrayAdapter<String> adapter;
    ListView listView;
    EditText ed;
    ImageButton addb;
    ImageButton deleteb;
    ImageButton settingb;
    TextView weather;
    Button complete;

    int count;
    long now = System.currentTimeMillis();
    Date date = new Date(now);
    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일");
    String formatDate = sdfNow.format(date);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todolist);

        dateNow = (TextView) findViewById(R.id.day);
        dateNow.setText(formatDate);

        //sharedpreference에 저장한 이름 불러오기, 텍스트에 set
        nametext = findViewById(R.id.name);
        SharedPreferences userinfo = getSharedPreferences("userinfo", MODE_PRIVATE);
        String inputName = userinfo.getString("username", "");
        nametext.setText(inputName + "의 집지키미 LIST");

        String inputPet = userinfo.getString("userpet", "");//반려동물 여부 가져오기
        String againstart=userinfo.getString("againStart", "");//againstart에 저장된 값으로 몇번째 실행인지 구분
        if (againstart.equals("yes")) {
            //한 번 이상 설정을 마쳤을때
            items=getStringArrayPref(SETTINGS_PLAYER_JSON);//저장된 list 가져오기
        }
        else { //첫번째 실행 시 기본 list 설정( item에 add)
            SharedPreferences.Editor editor= userinfo.edit();
            editor.putString("againStart", "yes");
            editor.commit();
            //앱 설치 후 첫 접속
            items = new ArrayList<>();
            items.add("보일러 끄기");
            items.add("전기장판 끄기");
            items.add("가스밸브 잠그기");
            items.add("방, 화장실 불 끄기");
            items.add("창문 닫기");
            items.add("우산 챙기기");
            //반려동물 여부 Yes일 시 item 추가
            if (inputPet.equals("Yes")) {
                items.add("반려동물 밥 주기");
                items.add("반려동물 배변 치우기");
            }
            setStringArrayPref(SETTINGS_PLAYER_JSON, items);//리스트 최초 설정

        }

        //어댑터 생성
        adapter = new ArrayAdapter<String>(activity_todolist.this, android.R.layout.simple_list_item_multiple_choice, items);

        //어댑터 설정
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); //여러 항목을 선택할 수 있도록 설정



        count = adapter.getCount();
        countText = (TextView) findViewById(R.id.count);
        countText.setText("할 일 : " + count + "개");


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override   // position 으로 몇번째 것이 선택됐는지 값을 넘겨준다
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(listView.isItemChecked(position)==true){
                    count--;
                    countText.setText("할 일 : " + count + "개");
                }
                if (listView.isItemChecked(position)==false) {
                    count++;
                    countText.setText("할 일 : "+count+"개");
                }

                if (count==0){
                    Intent intent = new Intent(getApplicationContext(), activity_finish.class);
                    startActivity(intent);
                }
            }
        });

        //add버튼
        addb = (ImageButton) findViewById(R.id.addb);

        addb.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = adapter.getCount();
                ed = (EditText) findViewById(R.id.newitem);
                String text = ed.getText().toString(); //EditText에 입력된 문자열 값을 얻기
                if (!text.isEmpty()) { //입력된 text문자열이 비어있지 않으면
                    items.add(text); //items 리스트에 입력된 문자열 추가
                    ed.setText(""); //EditText 입력란 초기화
                    adapter.notifyDataSetChanged(); //리스트 목록 갱신
                    count++;
                    countText.setText("할 일 : " + count + "개");
                    setStringArrayPref(SETTINGS_PLAYER_JSON, items);
                }
            }
        });

        //delete 버튼
        deleteb = (ImageButton) findViewById(R.id.deleteb);
        deleteb.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                count = adapter.getCount();
                SparseBooleanArray sbArray = listView.getCheckedItemPositions(); //선택된 아이템의 위치를 알려주는 배열
                Log.d("activity_todolist", sbArray.toString());
                if (sbArray.size() != 0) {
                    for (int i = listView.getCount() - 1; i >= 0; i--) {//목록의 역순으로 순회하면서 항목 제거
                        if (sbArray.get(i)) {
                            items.remove(i);
                            count--;
                            countText.setText("할 일 : " + count + "개");
                        }
                    }
                    listView.clearChoices();
                    adapter.notifyDataSetChanged();
                    setStringArrayPref(SETTINGS_PLAYER_JSON, items);
                }
            }
        });

        //설정 버튼
        settingb = findViewById(R.id.settingb);
        settingb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), activity_setting.class);
                startActivity(intent);
            }
        });


        //날씨
        weather = findViewById(R.id.weather);
        weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), activity_fagment.class);
                startActivity(intent);
            }
        });
    }

    //sharedpreference에 arraylist로 값 저장하기 위한 함수
    public void setStringArrayPref(String key, ArrayList<String> values) {
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PLAYER, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        //jsonarray 사용
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i)); // jsonarray에 값 저장
        }
        if (!values.isEmpty()) { //기존 key에 저장된 값 지우고 새로 셋팅
            editor.remove(key);
            editor.putString(key, a.toString());
        } else { //저장 값 없으면 key null
            editor.putString(key, null);
        }
        editor.commit();
    }

    //sharedpreference에서 arraylist로 저장된 값 불러오기 위한 함수
    public ArrayList<String> getStringArrayPref(String key) {
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PLAYER, MODE_PRIVATE);
        String json = prefs.getString(key, null);
        //jsonarray를 string 값으로 변환, 리턴
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
           try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}