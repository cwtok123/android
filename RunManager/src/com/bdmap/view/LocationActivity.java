package com.bdmap.view;

import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;


public class LocationActivity extends Activity implements OnClickListener {

	private MapView mapview;
	private BaiduMap bdMap;

	private LocationClient locationClient;
	private BDLocationListener locationListener;
	private BDNotifyListener notifyListener;

	private double longitude;// 经度
	private double latitude;// 纬度
	private float radius;// 定位精度半径，单位是米
	private String addrStr;// 反地理编码
	private String province;// 省份信息
	private String city;// 城市信息
	private String district;// 区县信息
	private float direction;// 手机方向信息
	private int locType;//定位模式

	private TextView speedInfo;
	// 定位按钮
	private Button locateBtn;
	// 定位模式 （普通-跟随-罗盘）
	private MyLocationConfiguration.LocationMode currentMode;
	// 定位图标描述
	private BitmapDescriptor currentMarker = null;
	// 记录是否第一次定位
	private boolean isFirstLoc = true;
	
	//链表用于画轨迹
	List<LatLng> pointstwo = new ArrayList<LatLng>();
	LatLng p1;
	LatLng p2;
	
	public double length=0.0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);

		mapview = (MapView) findViewById(R.id.bd_mapview);
		bdMap = mapview.getMap();
		locateBtn = (Button) findViewById(R.id.locate_btn);
		locateBtn.setOnClickListener(this);
		currentMode = MyLocationConfiguration.LocationMode.NORMAL;
		locateBtn.setText("普通");
		init();
		
	}

	/**
	 * 
	 */
	private void init() {
		bdMap.setMyLocationEnabled(true);
		// 1. 初始化LocationClient类
		locationClient = new LocationClient(getApplicationContext());
		// 2. 声明LocationListener类
		locationListener = new MyLocationListener();
		// 3. 注册监听函数
		locationClient.registerLocationListener(locationListener);
		// 4. 设置参数
		LocationClientOption locOption = new LocationClientOption();
		locOption.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		locOption.setCoorType("bd09ll");// 设置定位结果类型
		locOption.setScanSpan(2000);// 设置发起定位请求的间隔时间,ms
		locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向

		locationClient.setLocOption(locOption);
		locationClient.registerNotify(notifyListener);
		// 关闭 定位SDK
		locationClient.start();
	}

	/**
	 * 
	 * @author ys
	 *
	 */
	class MyLocationListener implements BDLocationListener {
		// 异步返回的定位结果
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			locType = location.getLocType();
			//Toast.makeText(LocationActivity.this, "当前定位的返回值是："+locType, Toast.LENGTH_SHORT).show();
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			if (location.hasRadius()) {// 判断是否有定位精度半径
				radius = location.getRadius();
			}
			if (locType == BDLocation.TypeGpsLocation) {
				//使用GPS定位时显示运动速度
				speedInfo = (TextView)findViewById(R.id.speedInfo);
				speedInfo.setText("运动速度：" + location.getSpeed());
				//Toast.makeText(LocationActivity.this,"速度：" + location.getSpeed() + " 卫星数：" + location.getSatelliteNumber(),Toast.LENGTH_SHORT).show();
			} else if (locType == BDLocation.TypeNetWorkLocation) {
				addrStr = location.getAddrStr();// 获取反地理编码(文字描述的地址)
				//Toast.makeText(LocationActivity.this, addrStr,Toast.LENGTH_SHORT).show();
			}
			direction = location.getDirection();// 获取手机方向，【0~360°】,手机上面正面朝北为0°
			province = location.getProvince();// 省份
			city = location.getCity();// 城市
			district = location.getDistrict();// 区县
			//Toast.makeText(LocationActivity.this,province + " " + city + " " + district, Toast.LENGTH_SHORT).show();
			// 构造定位数据
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(radius)//
					.direction(direction)// 方向
					.latitude(latitude)//
					.longitude(longitude)//
					.build();
			// 设置定位数据
			bdMap.setMyLocationData(locData);
			
			//绘制GPS变化点
			if (isFirstLoc) {
				//绘制第一个点
				isFirstLoc = false;
				p1 = p2 = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(p2);
				OverlayOptions pointFirst = new DotOptions().center(p1).radius(10)
						.color(0xFF0000FF);
				bdMap.addOverlay(pointFirst);
				bdMap.animateMapStatus(u);
			} else {
				//绘制其余点
				p2 = new LatLng(location.getLatitude(), location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(p2);
				OverlayOptions point = new DotOptions().center(p2).radius(6)
						.color(0xAAFF0000);
				bdMap.addOverlay(point);
				
				double distance = DistanceUtil.getDistance(p1, p2);
				length += distance;
				//绘制移动大于distance的连线
				if(distance>2){					
					//speedInfo.setText("距离"+distance);  //不知为何此句会造成程序崩溃
					pointstwo.add(p1);
					pointstwo.add(p2);
					OverlayOptions ooPolyline = new PolylineOptions().width(4)
							.color(0xAAFF0000).points(pointstwo);
					bdMap.addOverlay(ooPolyline);
					p1 = p2;
					//bdMap.animateMapStatus(u);
				}

			}
			

		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.locate_btn:// 定位
			switch (currentMode) {
			case NORMAL:
				locateBtn.setText("跟随");
				currentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
				break;
			case FOLLOWING:
				locateBtn.setText("罗盘");
				currentMode = MyLocationConfiguration.LocationMode.COMPASS;
				break;
			case COMPASS:
				locateBtn.setText("普通");
				currentMode = MyLocationConfiguration.LocationMode.NORMAL;
				break;
			}
			bdMap.setMyLocationConfigeration(new MyLocationConfiguration(
					currentMode, true, currentMarker));
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mapview.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapview.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapview.onDestroy();
		locationClient.unRegisterLocationListener(locationListener);
		locationClient.stop();
	}
}
