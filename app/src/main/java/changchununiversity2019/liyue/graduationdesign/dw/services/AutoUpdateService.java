package changchununiversity2019.liyue.graduationdesign.dw.services;

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

import com.google.gson.Gson;

import java.io.IOException;

import changchununiversity2019.liyue.graduationdesign.dw.R;
import changchununiversity2019.liyue.graduationdesign.dw.activities.WeatherActivity;
import changchununiversity2019.liyue.graduationdesign.dw.util.HttpUtil;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    private boolean autoUpdate = true;
    private boolean pushInfo = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        autoUpdate = sharedPreferences.getBoolean("autoUpdate", true);
        pushInfo = sharedPreferences.getBoolean("pushInfoSet",true);
        if (autoUpdate) {
            requestWeather();
            updateBingPic();
            if(pushInfo){
                pushNotification("每日天气","最新的天气信息已经更新！",3,true);
            }
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 4 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, i, 0);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);

        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * 根据天气ID请求城市天气数据
     */
    public void requestWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String weatherId = sharedPreferences.getString("weather_id", "CN101010100");
        HeConfig.init("HE1904031334171694", "da8bfbbb1dc24b9cb30e4b832c4e6f32");
        HeConfig.switchToFreeServerNode();
        HeWeather.getWeather(this, weatherId, new HeWeather.OnResultWeatherDataListBeansListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(interfaces.heweather.com.interfacesmodule.bean.weather.Weather weather) {
                String weatherString = new Gson().toJson(weather);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("weather", weatherString);
                editor.apply();
                requestAqi(weatherId);
            }
        });
    }

    /**
     * 查询当前城市空气质量。
     *
     * @param weatherId
     */
    public void requestAqi(final String weatherId) {
        HeConfig.switchToFreeServerNode();
        HeWeather.getAirNow(this, weatherId, new HeWeather.OnResultAirNowBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("weather_id", weatherId);
                editor.putString("aqi", null);
                editor.apply();
            }

            @Override
            public void onSuccess(AirNow airNow) {
                String aqiString = new Gson().toJson(airNow);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("weather_id", weatherId);
                editor.putString("aqi", aqiString);
                editor.apply();
            }
        });

    }

    /**
     * 更新背景图片。
     */
    private void updateBingPic() {
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
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }

    private void pushNotification(String title, String message, int notifyId, boolean autoCancle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "daily_weather";//设置通道的唯一ID
            String channelName = "每日天气";//设置通道名
            int importance = NotificationManager.IMPORTANCE_HIGH;//设置通道优先级
            createNotificationChannel(channelId, channelName, importance, title, message, notifyId, autoCancle);
        } else {
            sendSubscribeMsg(title, message, notifyId, autoCancle);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance, String title, String message, int notifyId, boolean autoCancle) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        sendSubscribeMsg(title, message, notifyId, autoCancle);
    }

    public void sendSubscribeMsg(String title, String message, int notifyId, boolean autoCancle) {
        Intent intent = new Intent(this, WeatherActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "daily_weather")
                .setContentTitle(title)
                .setContentText(message)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.weather)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.weather))
                .setAutoCancel(autoCancle)
                .setContentIntent(pendingIntent)
                .build();
        if (!autoCancle) {
            notification.flags |= Notification.FLAG_NO_CLEAR;
        }
        manager.notify(notifyId, notification);
    }

}
