package changchununiversity2019.liyue.graduationdesign.dw.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.ArrayList;
import java.util.List;

import changchununiversity2019.liyue.graduationdesign.dw.R;

public class LBSTest extends AppCompatActivity {

    public LocationClient locationClient;
    private TextView positionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());

        setContentView(R.layout.activity_lbstest);

        positionText = (TextView)findViewById(R.id.position_text_view);
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(LBSTest.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(LBSTest.this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(LBSTest.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String []permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(LBSTest.this,permissions,1);
        }else{
            requestLocation();
        }

    }
    private void requestLocation(){
        initLocation();
        locationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);//是否需要具体位置信息
        locationClient.setLocOption(option);
    }

    public void onRequestPermissionResult(int requestCode,String []permissions,int []grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本软件！",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this,"发生未知错误！",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            default:
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        locationClient.stop();
    }

    public class MyLocationListener implements BDLocationListener {
        public void onReceiveLocation(final BDLocation location){
            runOnUiThread(new Runnable(){
                public void run(){
                    StringBuilder currentPostion = new StringBuilder();
                    currentPostion.append("纬度：").append(location.getLatitude()).append("\n");
                    currentPostion.append("经度：").append(location.getLongitude()).append("\n");
                    currentPostion.append("国家：").append(location.getCountry()).append("\n");
                    currentPostion.append("省份：").append(location.getProvince()).append("\n");
                    currentPostion.append("市级：").append(location.getCity()).append("\n");
                    currentPostion.append("区级：").append(location.getDistrict()).append("\n");
                    currentPostion.append("街道：").append(location.getStreet()).append("\n");
                    currentPostion.append("定位方式：");
                    if(location.getLocType() == BDLocation.TypeGpsLocation){
                        currentPostion.append("GPS");
                    }else if(location.getLocType() == BDLocation.TypeNetWorkLocation){
                        currentPostion.append("网络");
                    }
                    positionText.setText(currentPostion);
                }
            });
        }

        public void onConnectHotSpotMessage(String s,int i){

        }
    }
}
