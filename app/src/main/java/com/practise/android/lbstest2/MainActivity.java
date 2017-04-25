package com.practise.android.lbstest2;

import android.Manifest;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    public LocationClient mLocationClient;

    private TextView positionText;

    public static final int UPDATE_TEXT = 1;

    private MapView mapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 创建一个 LocationClient 实例，并令其构造函数接收一个 Context 参数
        // 用 getApplicationContext() 获取该全局的Context参数并传入
        mLocationClient = new LocationClient(getApplicationContext());
        // 调用 LocationClient 的 registerLocationListener() 方法来注册一个定位监听器
        // 当获取到未知信息时，就会回调这个定位监听器
        mLocationClient.registerLocationListener(new MyLocationListener());
        // 初始化操作：接收一个全局的Context参数（必需在setContentView()之前调用）
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        positionText = (TextView) findViewById(R.id.position_text_view);
        mapView = (MapView) findViewById(R.id.bmapView);

        // 创建一个 List 集合，依次判断三个权限有没有被授权，如果没被授权就添加到 List 集合中
        // 最后将 List 转换成数组，再调用 ActivityCompat.requestPermissions() 方法一次性申请
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }


    }

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    StringBuilder address = (StringBuilder) msg.obj;
                    positionText.setText(address);
            }
        }
    };

    // 调用 LocationClint 的 start() 方法开始定位，
    // 定位结果会回调到之前注册的监听器 MyLocationListener 中
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        // option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    public class MyLocationListener implements BDLocationListener {

        int i = 1;

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(location.getLatitude()).
                    append("\n");
            currentPosition.append("经线：").append(location.getLongitude()).
                    append("\n");
            currentPosition.append("国家：").append(location.getCountry()).
                    append("\n");
            currentPosition.append("省：").append(location.getProvince()).
                    append("\n");
            currentPosition.append("市：").append(location.getCity()).
                    append("\n");
            currentPosition.append("区：").append(location.getDistrict()).
                    append("\n");
            currentPosition.append("街道：").append(location.getStreet()).
                    append("\n");
            currentPosition.append("定位方式：");
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
            }
            // positionText.setText(currentPosition);
            // Toast.makeText(MainActivity.this, currentPosition, Toast.LENGTH_LONG).show();
            Message message = new Message();
            message.what = UPDATE_TEXT;
            message.obj = currentPosition;
            handler.sendMessage(message);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }


}
