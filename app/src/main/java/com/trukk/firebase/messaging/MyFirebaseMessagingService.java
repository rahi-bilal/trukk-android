package com.trukk.firebase.messaging;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.trukk.MainActivity;
import com.trukk.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "TAG";
    String body_msg;
    int count;

    ArrayList<HashMap<String, String>> arrayList_friend;
    SharedPreferences db;
    @Override
    public void onNewToken(String mToken) {
        super.onNewToken(mToken);
        Log.e("TOKEN",mToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "coming: "+remoteMessage.getData());

        sendNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());

        /*if (remoteMessage.getData().size() > 0) {
            String data_string = remoteMessage.getData().toString();
            JSONObject jobject = null;
            try {
                jobject = new JSONObject(data_string);
                String title = jobject.optString("title");
                JSONObject obj = jobject.optJSONObject("body");
                String body = obj.optString("body");
                String date = parseDate(obj.optString("date"));
                String comment = obj.optString("comment");
                String comment_id = obj.optString("comment_id");
                String typee = obj.optString("type");
                sendNotification(body);


                HashMap<String, String > map = new HashMap<>();
                map.put("title" , title);
                map.put("body" , body);
                map.put("date" , date);
                if(body.equals("Your comment has been selected for poll")){
                    map.put("type" , "put poll");
                    map.put("comment" , comment);
                    map.put("comment_id" , comment_id);
                    map.put("typee" , typee);
                }else{
                    map.put("type" , "normal");
                }

            }catch (JSONException e) {
                e.printStackTrace();
            }

        }*/
    }


    private void handleCount(){
        Log.e(TAG, "received broadcast");
        Intent intent = new Intent("intentFCM");
        intent.putExtra("count",count+"");
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }


    private void sendNotification(String title,String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        String channelId ="468458";
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(messageBody));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


    public String parseDate(String time) {
        //2019-01-10 01:07:00pm
        String inputPattern = "yyyy-MM-dd hh:mm:ssa";
        String outputPattern = "dd MMM, yyyy hh:mma";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.ENGLISH);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

}
