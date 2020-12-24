package org.techtown.termproject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


import static android.content.Context.MODE_PRIVATE;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver(){ }

    NotificationManager manager;
    NotificationCompat.Builder builder;

    //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "Channel1";

    @Override
    public void onReceive(Context context, Intent intent) {
        //notification service 설정
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        builder = null;
        manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        //OREO API 26 이상에서는 채널 설정 필요함.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        //알림창 클릭 시 앱 실행. mainactivity 화면 부름
        Intent intent2 = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,101,intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle("집지키미");
        builder.setContentText("잠깐! 확인하셨나요?");//알림창 아이콘. 안드로이드 기기 연결시 알림창에 home 그림이 나타난다.
        //nox에서는 home 그림이 제대로 보이지 않는다.
        builder.setSmallIcon(R.drawable.home);//알림창 클릭 시 자동으로 삭제함.
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);//설정한 알림창 notification에 build
        Notification notification = builder.build();
        manager.notify(1,notification);

        //내일 같은 시간으로 알람시간 결정 -> 매일 동일시간에 알람
        Calendar nextNotifyTime = Calendar.getInstance();
        nextNotifyTime.add(Calendar.DATE, 1);
        //sharedpreference에 설정한 값 저장
        SharedPreferences.Editor editor = context.getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
        editor.putLong("nextNotifyTime", nextNotifyTime.getTimeInMillis());
        editor.apply();

        Toast.makeText(context.getApplicationContext(),"[집지키미] 알림 확인 필요, 내일도 알림이 울립니다.", Toast.LENGTH_SHORT).show();

    }
}