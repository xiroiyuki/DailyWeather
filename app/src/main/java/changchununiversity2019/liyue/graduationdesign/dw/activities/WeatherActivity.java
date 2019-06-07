package changchununiversity2019.liyue.graduationdesign.dw.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.PendingIntent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import changchununiversity2019.liyue.graduationdesign.dw.R;
import changchununiversity2019.liyue.graduationdesign.dw.db.City;
import changchununiversity2019.liyue.graduationdesign.dw.db.County;
import changchununiversity2019.liyue.graduationdesign.dw.db.Province;
import changchununiversity2019.liyue.graduationdesign.dw.gson.AQI;
import changchununiversity2019.liyue.graduationdesign.dw.gson.Forecast;
import changchununiversity2019.liyue.graduationdesign.dw.gson.Suggestion;
import changchununiversity2019.liyue.graduationdesign.dw.gson.Weather;
import changchununiversity2019.liyue.graduationdesign.dw.services.AutoUpdateService;
import changchununiversity2019.liyue.graduationdesign.dw.util.ActivityCollector;
import changchununiversity2019.liyue.graduationdesign.dw.util.HttpUtil;
import changchununiversity2019.liyue.graduationdesign.dw.util.Utility;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends BaseActivity {
    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private TextView airText;

    private TextView dressText;
    private TextView fluText;
    private TextView travelText;
    private TextView uvText;
    private TextView airPollutionText;

    private TextView feelTmpText;
    private TextView humidityText;
    private TextView windDirText;
    private TextView windFourceText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefreshLayout;

    private String mWeatherId;

    private Button navButton;

    /*添加功能*/
    /*推送通知*/
    public boolean pushNoti = true;
    public boolean GPSLocation = true;
    public boolean autoUpdateInfo = true;

    private volatile String baiduProvinceName = "";
    private volatile String baiduCityName = "";
    private volatile String baiduCountyName = "";

    private LocationClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        /*使背景与手机状态栏融合在一起*/
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        /**读取Preference**/
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        GPSLocation = sharedPreferences.getBoolean("acquireLocation", true);
        pushNoti = sharedPreferences.getBoolean("pushInfoSet", true);
        autoUpdateInfo = sharedPreferences.getBoolean("autoUpdate", true);

        /**申请权限**/
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this, permissions, 1);
        } else {

            if (GPSLocation) {
                if (locationClient != null) {
                    requestLocation();
                } else {
                    locationClient = new LocationClient(WeatherActivity.this);
                    locationClient.registerLocationListener(new MyLocationListener());
                    requestLocation();
                }
            }

        }

        setContentView(R.layout.activity_weather);

        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        airText = findViewById(R.id.air_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);

        dressText = findViewById(R.id.dress_text);
        fluText = findViewById(R.id.flu_text);
        travelText = findViewById(R.id.travel_text);
        uvText = findViewById(R.id.uv_text);
        airPollutionText = findViewById(R.id.air_pollution_text);

        feelTmpText = findViewById(R.id.feel_tmp_text);
        humidityText = findViewById(R.id.hum_text);
        windDirText = findViewById(R.id.wind_dir_text);
        windFourceText = findViewById(R.id.wind_power_text);

        bingPicImg = findViewById(R.id.bing_pic_img);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        navButton = (Button) findViewById(R.id.nav_button);

        String weatherString = sharedPreferences.getString("weather", null);
        String aqiString = sharedPreferences.getString("aqi", null);
        mWeatherId = sharedPreferences.getString("weather_id", "CN101010100");
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            if (mWeatherId == weatherId) {
                AQI aqi = null;
                if (aqiString != null) {
                    aqi = Utility.handleAQIResponse(aqiString);
                }
                showWeatherInfo(weather, aqi);
            } else {
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }
        } else {
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }


        String bingPic = sharedPreferences.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                SharedPreferences checkPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                GPSLocation = checkPreferences.getBoolean("acquireLocation", true);
                pushNoti = checkPreferences.getBoolean("pushInfoSet", true);
                autoUpdateInfo = checkPreferences.getBoolean("autoUpdate", true);
                if (GPSLocation) {
                    locationClient = new LocationClient(WeatherActivity.this);
                    locationClient.registerLocationListener(new MyLocationListener());
                    requestLocation();
                } else {
                    mWeatherId = checkPreferences.getString("weather_id", "CN101010100");
                    requestWeather(mWeatherId);
                }
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(WeatherActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

    }

    /*定位部分*/
    private void requestLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);//是否需要具体位置信息
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setScanSpan(5000);
        locationClient.setLocOption(option);
        locationClient.start();
    }

    public class MyLocationListener implements BDLocationListener {
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (Objects.isNull(location)) {
                        return;
                    }

                    baiduProvinceName = location.getProvince();
                    baiduCityName = location.getCity();
                    baiduCountyName = location.getDistrict();

                    new CheckTask().execute();

                }
            });
        }

    }

    public class CheckTask extends AsyncTask<Void, Integer, Integer> {
        private static final int TYPE_SUCCESS = 0;
        private static final int TYPE_FAILED = 1;

        private List<Province> allProvinces;
        private List<City> allCities;
        private List<County> allCounties;

        private List<Province> targetProvince;
        private List<City> targetCity;
        private List<County> targetCounty;
        private Province baiduProvince = null;
        private City baiduCity = null;
        private County baiduCounty = null;

        private ProgressDialog taskDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showTaskDialog();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            closeTaskDialog();
            switch (integer) {
                case TYPE_FAILED:
                    Toast.makeText(WeatherActivity.this, "获取定位位置天气信息失败！可以尝试刷新，再次获取。", Toast.LENGTH_SHORT).show();

                    break;

                case TYPE_SUCCESS:
                    if (locationClient != null) {
                        locationClient.stop();
                    }
                    requestWeather(baiduCounty.getWeatherId());
                    Toast.makeText(WeatherActivity.this, "即将为您显示当前所在位置天气信息！", Toast.LENGTH_SHORT).show();

                    break;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            baiduCounty = null;
            checkProvince(baiduProvinceName);
            if (baiduCounty != null) {
                return TYPE_SUCCESS;
            }
            return TYPE_FAILED;

        }

        /**
         * Start 核对位置信息
         **/
        private void checkProvince(String provinceName) {
            if(provinceName != null){
                if (provinceName.endsWith("省")) {
                    provinceName = provinceName.substring(0, provinceName.length() - 1);
                }
                allProvinces = DataSupport.findAll(Province.class);
                if (allProvinces.size() > 0) {
                    targetProvince = DataSupport.where("provincename = ?", provinceName).find(Province.class);
                    if (targetProvince.size() > 0) {
                        baiduProvince = targetProvince.get(0);
                    } else {
                        baiduProvince = allProvinces.get(0);
                    }

                    checkCity(baiduCityName);

                } else {
                    String address = "http://guolin.tech/api/china";
                    queryAllFromServer(address, "province", "");
                }
            }
        }

        private void checkCity(String cityName) {
            if (cityName.endsWith("市")) {
                cityName = cityName.substring(0, cityName.length() - 1);
            }
            allCities = DataSupport.where("provinceid = ?", String.valueOf(baiduProvince.getProvinceCode())).find(City.class);
            if (allCities.size() > 0) {
                targetCity = DataSupport.where("cityname = ? and provinceid = ?", cityName, String.valueOf(baiduProvince.getProvinceCode())).find(City.class);
                if (targetCity.size() > 0) {
                    baiduCity = targetCity.get(0);
                } else {
                    baiduCity = allCities.get(0);
                }
                checkCounty(baiduCountyName);
            } else {
                String address = "http://guolin.tech/api/china/" + baiduProvince.getProvinceCode();
                queryAllFromServer(address, "city", baiduProvince);
            }
        }

        private void checkCounty(String countyName) {
            if (countyName.endsWith("区") || countyName.endsWith("县")) {
                countyName = countyName.substring(0, countyName.length() - 1);
            }
            allCounties = DataSupport.where("cityid = ?", String.valueOf(baiduCity.getId())).find(County.class);
            if (allCounties.size() > 0) {
                targetCounty = DataSupport.where("countyname = ? and cityid = ?", countyName, String.valueOf(baiduCity.getId())).find(County.class);
                if (targetCounty.size() > 0) {
                    baiduCounty = targetCounty.get(0);
                } else {
                    baiduCounty = allCounties.get(0);
                }
            } else {
                String address = "http://guolin.tech/api/china/" + baiduCity.getProvinceId() + "/" + baiduCity.getCityCode();
                queryAllFromServer(address, "county", baiduCity);
            }

        }

        private void queryAllFromServer(String address, final String type, final Object object) {

            HttpUtil.sendOkHttpRequest(address, new Callback() {
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    boolean result = false;
                    if ("province".equals(type)) {
                        result = Utility.handleProvinceResponse(responseText);
                    } else if ("city".equals(type)) {
                        result = Utility.handleCityResponse(responseText, ((Province) object).getId());
                    } else if ("county".equals(type)) {
                        result = Utility.handleCountyResponse(responseText, ((City) object).getId());
                    }
                    if (result) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if ("province".equals(type)) {
                                    checkProvince(baiduProvinceName);
                                } else if ("city".equals(type)) {
                                    checkCity(baiduCityName);
                                } else if ("county".equals(type)) {
                                    checkCounty(baiduCountyName);
                                }
                            }
                        });
                    }
                }

                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(WeatherActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

        /*check end*/
        private void showTaskDialog() {
            if (taskDialog == null || !taskDialog.isShowing()) {
                taskDialog = new ProgressDialog(WeatherActivity.this);
                taskDialog.setMessage("正在匹配您的地理信息......");
                taskDialog.setCanceledOnTouchOutside(false);
            }

        }

        private void closeTaskDialog() {
            if (taskDialog != null) {
                taskDialog.dismiss();
            }
        }
    }


    /**
     * 加载背景图片。
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 根据天气ID请求城市天气数据
     *
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        HeConfig.init("HE1904031334171694", "da8bfbbb1dc24b9cb30e4b832c4e6f32");
        HeConfig.switchToFreeServerNode();
        HeWeather.getWeather(WeatherActivity.this, weatherId, new HeWeather.OnResultWeatherDataListBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(WeatherActivity.this, "获取天气数据失败!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(interfaces.heweather.com.interfacesmodule.bean.weather.Weather weather) {
                if (weather != null && weather.getStatus().equals("ok")) {
                    String weatherString = new Gson().toJson(weather);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("weather", weatherString);
                    editor.apply();
                    requestAqi(weatherId);
                } else {

                    Toast.makeText(WeatherActivity.this, "获取天气数据失败!", Toast.LENGTH_LONG).show();
                }
            }
        });
        loadBingPic();
    }

    /**
     * 查询当前城市空气质量。
     *
     * @param weatherId
     */
    public void requestAqi(final String weatherId) {
        HeConfig.switchToFreeServerNode();
        HeWeather.getAirNow(WeatherActivity.this, weatherId, new HeWeather.OnResultAirNowBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("weather_id", weatherId);
                editor.putString("aqi", null);
                editor.apply();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String weatherString = sharedPreferences.getString("weather", null);
                Weather weather = Utility.handleWeatherResponse(weatherString);
                AQI aqi = null;
                showWeatherInfo(weather, aqi);
                Toast.makeText(WeatherActivity.this, "获取空气质量数据失败!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(AirNow airNow) {
                String aqiString = new Gson().toJson(airNow);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("weather_id", weatherId);
                editor.putString("aqi", aqiString);
                editor.apply();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String weatherString = sharedPreferences.getString("weather", null);
                Weather weather = Utility.handleWeatherResponse(weatherString);
                AQI aqi = Utility.handleAQIResponse(aqiString);
                showWeatherInfo(weather, aqi);
            }
        });
    }


    /**
     * 处理并保存Weather实体类中的数据。
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather, AQI aqi) {

        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.weatherCondition;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        feelTmpText.setText(weather.now.feelTemperature + "℃");
        humidityText.setText(weather.now.humidity);
        windDirText.setText(weather.now.windDir);
        windFourceText.setText(weather.now.windPower);

        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.dayCondition);
            maxText.setText(forecast.temperatureMax);
            minText.setText(forecast.temperatureMin);
            forecastLayout.addView(view);
        }

        if (aqi != null) {
            aqiText.setText(aqi.more.aqi);
            pm25Text.setText(aqi.more.pm25);
            airText.setText(aqi.more.quality);
        } else {
            aqiText.setText("暂无数据");
            pm25Text.setText("暂无数据");
            airText.setText("暂无数据");
        }


        String comfort = "";
        for (Suggestion suggestion : weather.suggestions) {
            if (suggestion.type.equals("comf")) {
                comfort = "舒适度：" + suggestion.text;
                comfortText.setText(comfort);
            } else if (suggestion.type.equals("drsg")) {
                dressText.setText("穿衣建议：" + suggestion.text);
            } else if (suggestion.type.equals("flu")) {
                fluText.setText("感冒机率：" + suggestion.text);
            } else if (suggestion.type.equals("sport")) {
                sportText.setText("运动建议：" + suggestion.text);
            } else if (suggestion.type.equals("trav")) {
                travelText.setText("旅游建议：" + suggestion.text);
            } else if (suggestion.type.equals("uv")) {
                uvText.setText("紫外线：" + suggestion.text);
            } else if (suggestion.type.equals("cw")) {
                carWashText.setText("洗车建议：" + suggestion.text);
            } else if (suggestion.type.equals("air")) {
                airPollutionText.setText("空气污染扩散条件指数：" + suggestion.text);
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);

        if(pushNoti){
            pushNotification(cityName + " " + degree, comfort, 2, true);
        }
        swipeRefreshLayout.setRefreshing(false);
        if (autoUpdateInfo) {
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        }
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

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本软件！", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }

                    if (GPSLocation) {
                        if (locationClient != null) {
                            requestLocation();
                        } else {
                            locationClient = new LocationClient(WeatherActivity.this);
                            locationClient.registerLocationListener(new MyLocationListener());
                            requestLocation();
                        }
                    }

                } else {
                    Toast.makeText(this, "发生未知错误！", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            default:
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (locationClient != null) {
            locationClient.stop();
        }

    }


    public void onBackPressed() {
        ActivityCollector.finishAll();
    }

}
