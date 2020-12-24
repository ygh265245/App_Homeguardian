package org.techtown.termproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class activity_time extends AppCompatActivity {

    private AlarmManager alarmManager;
    private GregorianCalendar mCalender;

    private NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time);
// 화면으로 넘어가기

        Button nextbt=(Button)findViewById(R.id.nextbt);

        nextbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //첫 설정 후 재실행 시 설정화면이 나오지 않도록 한다
                Intent intent = new Intent(getApplicationContext(), activity_todolist.class);
                startActivity(intent);
            }
        });


        //타임피커 셋팅
        final TimePicker picker=(TimePicker)findViewById(R.id.timePicker);
        picker.setIs24HourView(true);

        // 앞서 설정한 값으로 보여주기
        // 없으면 디폴트 값은 현재시간
        SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
        long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());
        Calendar nextNotifyTime = new GregorianCalendar();
        nextNotifyTime.setTimeInMillis(millis);
        Date nextDate = nextNotifyTime.getTime();

        // 이전 설정값으로 TimePicker 초기화
        Date currentTime = nextNotifyTime.getTime();
        SimpleDateFormat HourFormat = new SimpleDateFormat("kk", Locale.getDefault());
        SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());
        int pre_hour = Integer.parseInt(HourFormat.format(currentTime));
        int pre_minute = Integer.parseInt(MinuteFormat.format(currentTime));

        if (Build.VERSION.SDK_INT >= 23 ){
            picker.setHour(pre_hour);
            picker.setMinute(pre_minute);
        }
        else{
            picker.setCurrentHour(pre_hour);
            picker.setCurrentMinute(pre_minute);
        }

        //시간 설정 시작
        Button timebt=(Button)findViewById(R.id.button6);
        timebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int hour, hour_24, minute;
                String am_pm;
                //타임피커 시간 int 값으로 get
                if (Build.VERSION.SDK_INT >= 23 ){
                    hour_24 = picker.getHour();
                    minute = picker.getMinute();
                }
                else{
                    hour_24 = picker.getCurrentHour();
                    minute = picker.getCurrentMinute();
                }
                if(hour_24 > 12) {
                    am_pm = "PM";
                    hour = hour_24 - 12;
                }
                else
                {
                    hour = hour_24;
                    am_pm="AM";
                }



                notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                mCalender = new GregorianCalendar();
                Intent receiverIntent = new Intent(activity_time.this, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(activity_time.this, 0, receiverIntent, 0);

                // 현재 지정된 시간으로 알람 시간 설정
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour_24);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1);
                }
                //  Preference에 설정한 값 저장
                SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                editor.putLong("nextNotifyTime", (long)calendar.getTimeInMillis());
                editor.apply();
                //toast 메시지로 출력
                Date nextDate = calendar.getTime();
                String date_text = new SimpleDateFormat("MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(nextDate);
                Toast.makeText(getApplicationContext(),"알람이 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();


                alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(),pendingIntent);




            }

        });
    }


}