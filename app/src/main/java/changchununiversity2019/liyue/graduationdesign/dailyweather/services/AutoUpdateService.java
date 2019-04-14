package changchununiversity2019.liyue.graduationdesign.dailyweather.services;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

import changchununiversity2019.liyue.graduationdesign.dailyweather.R;
import changchununiversity2019.liyue.graduationdesign.dailyweather.activities.WeatherActivity;
import changchununiversity2019.liyue.graduationdesign.dailyweather.gson.Weather;
import changchununiversity2019.liyue.graduationdesign.dailyweather.util.HttpUtil;
import changchununiversity2019.liyue.graduationdesign.dailyweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    boolean pushNoti = true;

    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    public int onStartCommand(Intent intent,int flags,int startId){

        updateWeather();
        updateBingPic();

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour = 3*60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,i,0);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);

        return super.onStartCommand(intent,flags,startId);
    }

    /**
     * 更新天气信息。
     */
    private void updateWeather(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        pushNoti = sharedPreferences.getBoolean("pushInfoSet",true);
        String weatherString = sharedPreferences.getString("weather",null);
        if(weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;

            String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=da8bfbbb1dc24b9cb30e4b832c4e6f32";

            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if(weather != null && "ok".equals(weather.status)){
                        pushNotification(weather);
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    /**
     * 更新背景图片。
     */
    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }

    /**
     * 推送最新天气通知。
     * @param weather
     */
    private void pushNotification(Weather weather){
        if(pushNoti && weather != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = "daily_weather";//设置通道的唯一ID
                String channelName = "每日天气";//设置通道名
                int importance = NotificationManager.IMPORTANCE_HIGH;//设置通道优先级
                createNotificationChannel(channelId, channelName, importance,weather);
            } else {
                sendSubscribeMsg(weather);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance,Weather weather) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        sendSubscribeMsg(weather);
    }
    public void sendSubscribeMsg(Weather weather) {
        Intent intent = new Intent(this, WeatherActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "daily_weather")
                .setContentTitle(weather.basic.cityName+" "+weather.now.temperature+"℃")
                .setContentText("舒适度："+weather.suggestion.comfort.info)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.weather)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.weather))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(2, notification);
    }
}
