package changchununiversity2019.liyue.graduationdesign.dailyweather.activities;

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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import changchununiversity2019.liyue.graduationdesign.dailyweather.R;
import changchununiversity2019.liyue.graduationdesign.dailyweather.db.City;
import changchununiversity2019.liyue.graduationdesign.dailyweather.db.County;
import changchununiversity2019.liyue.graduationdesign.dailyweather.db.Province;
import changchununiversity2019.liyue.graduationdesign.dailyweather.gson.Forecast;
import changchununiversity2019.liyue.graduationdesign.dailyweather.gson.Weather;
import changchununiversity2019.liyue.graduationdesign.dailyweather.services.AutoUpdateService;
import changchununiversity2019.liyue.graduationdesign.dailyweather.util.ActivityCollector;
import changchununiversity2019.liyue.graduationdesign.dailyweather.util.HttpUtil;
import changchununiversity2019.liyue.graduationdesign.dailyweather.util.MyApplication;
import changchununiversity2019.liyue.graduationdesign.dailyweather.util.Utility;
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

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefreshLayout;

    private String mWeatherId;

    public DrawerLayout drawerLayout;

    private Button navButton;

    /*添加功能*/
    /*推送通知*/
    public boolean pushNoti = true;
    public boolean GPSLocation = true;
    public boolean autoUpdateInfo = true;

    private List<Province> allProvinces;
    private List<City> allCities;
    private List<County> allCounties;

    private List<Province> targetProvince;
    private List<City> targetCity;
    private List<County> targetCounty;
    private Province baiduProvince = null;
    private City baiduCity = null;
    private County baiduCounty = null;

    private String baiduProvinceName = "";
    private String baiduCityName = "";
    private String baiduCountyName = "";

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
                locationClient = new LocationClient(this);
                locationClient.registerLocationListener(new MyLocationListener());
                requestLocation();
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

        bingPicImg = findViewById(R.id.bing_pic_img);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        //drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather", null);
        mWeatherId = sharedPreferences.getString("weather_id", "CN101010100");
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            if (mWeatherId == weatherId) {
                showWeatherInfo(weather);
                pushNotification(weather);
            } else {
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }
        } else {
            //mWeatherId = sharedPreferences.getString("weather_id", null);
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

    /**
     * 初始化信息
     **/
    private void initData() {
// TODO 定位完成之后重新加载 显示天气数据     网络请求一定是在子线程里面的！！！
        /*baiduProvince = null;
        baiduCity = null;
        baiduCounty = null;
        baiduProvinceName = "";
        baiduCityName = "";
        baiduCountyName = "";*/

    }


    /*定位部分*/
    private void requestLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);//是否需要具体位置信息
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setScanSpan(5000);
        //option.setOpenGps(false);
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
        private ProgressDialog taskDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showTaskDialog();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            switch (integer) {
                case TYPE_FAILED:
                    Toast.makeText(WeatherActivity.this, "获取天气信息失败！可以尝试刷新，再次获取。", Toast.LENGTH_LONG).show();

                    closeTaskDialog();
                    break;

                case TYPE_SUCCESS:
                    closeTaskDialog();
                    if (locationClient != null) {
                        locationClient.stop();
                    }
                    Toast.makeText(WeatherActivity.this, "为您显示当前所在位置天气信息！", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(true);
                    requestWeather(baiduCounty.getWeatherId());
                    break;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            checkProvince(baiduProvinceName);
            if (baiduCounty != null) {
                return TYPE_SUCCESS;
            } else {
                return TYPE_FAILED;
            }

        }

        /**
         * Start 核对位置信息
         **/
        private void checkProvince(String provinceName) {
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
            //showProgressDialog();
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
                                //closeProgressDialog();
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
                            //closeProgressDialog();
                            Toast.makeText(WeatherActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

        /*check end*/
        private void showTaskDialog() {
            if (taskDialog == null) {
                taskDialog = new ProgressDialog(WeatherActivity.this);
                taskDialog.setMessage("正在匹配您的地理信息......");
                taskDialog.setCanceledOnTouchOutside(false);
            }
            taskDialog.show();
        }

        private void closeTaskDialog() {
            if (taskDialog != null) {
                taskDialog.dismiss();
            }
        }
    }


    /**
     * 推送最新天气通知。
     *
     * @param weather
     */
    private void pushNotification(Weather weather) {
        if (pushNoti && weather != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = "daily_weather";//设置通道的唯一ID
                String channelName = "每日天气";//设置通道名
                int importance = NotificationManager.INTERRUPTION_FILTER_ALARMS;//设置通道优先级
                createNotificationChannel(channelId, channelName, importance, weather);
            } else {
                sendSubscribeMsg(weather);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance, Weather weather) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        sendSubscribeMsg(weather);
    }

    public void sendSubscribeMsg(Weather weather) {
        Intent intent = new Intent(this, WeatherActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "daily_weather")
                .setContentTitle(weather.basic.cityName + " " + weather.now.temperature + "℃")
                .setContentText("舒适度：" + weather.suggestion.comfort.info)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.weather)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.weather))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(2, notification);
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
     * 处理并保存Weather实体类中的数据。
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            airText.setText(weather.aqi.city.qlty);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        pushNotification(weather);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 根据天气ID请求城市天气数据
     *
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=da8bfbbb1dc24b9cb30e4b832c4e6f32";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            mWeatherId = weather.basic.weatherId;
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.putString("weather_id", mWeatherId);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            //Toast.makeText(WeatherActivity.this,"获取天气数据失败!",Toast.LENGTH_LONG).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }


            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        //Toast.makeText(WeatherActivity.this,"获取天气数据失败!",Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

        });

        loadBingPic();
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
                            locationClient = new LocationClient(getApplicationContext());
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
