package com.bdmap.view;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends Activity{
	private Button btn1;
	private Button btn2;
	private Button btn3;
	private Button btn4;
	private Button btn5;
	private TextView aqiText;
	private Chronometer timer;
	private long recordingTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btn1 = (Button) findViewById(R.id.button1);
		btn2 = (Button) findViewById(R.id.button2);
		btn3 = (Button) findViewById(R.id.button3);
		btn4 = (Button) findViewById(R.id.button4);
		btn5 = (Button) findViewById(R.id.button5);
		aqiText = (TextView) findViewById(R.id.aqiText);
		timer = (Chronometer)this.findViewById(R.id.chronometer1);
		/*
		try {
			URL url1 = new URL("http://www.pm25.in/api/querys/pm2_5.json?city=fuzhou&token=5j1znBVAsnSf5xQyNQyq");
			PM2 pm2 =Json.GetWebJson1(url1);
			aqi.setText("福州今天空气质量指数："+pm2.aqi+"/r空气质量："+pm2.quality);
		} catch (MalformedURLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			aqi.setText("网络错误");
			e.printStackTrace();
		}
		*/
		//直接在主线程进行网络请求会抛出NetworkOnMainThreadException异常
		//这里新建一个子线程，用Handler消息机制与主线程通信
		new Thread(networkTask).start(); 
		button();		
	}
	
	Handler handler = new Handler() {  
	    @Override  
	    public void handleMessage(Message msg) {  
	        super.handleMessage(msg);  
	        Bundle data = msg.getData();  
	        String aqi = data.getString("aqi"); 
	        String quality = data.getString("quality");
	        aqiText.setText("福州今天空气质量指数："+aqi+"\n空气质量："+quality);

	    }  
	}; 
	
	Runnable networkTask = new Runnable() {  
		  
	    @Override  
	    public void run() {
	    	try {
				URL url = new URL("http://www.pm25.in/api/querys/pm2_5.json?city=fuzhou&token=5j1znBVAsnSf5xQyNQyq");
				PM25 pm2 =Json.getWebJson(url);
				Message msg = new Message();  
		        Bundle data = new Bundle();  
		        data.putString("aqi", pm2.aqi);  
		        data.putString("quality", pm2.quality);
		        msg.setData(data);  
		        handler.sendMessage(msg); 
				//aqi.setText("福州今天空气质量指数："+pm2.aqi+"\n空气质量："+pm2.quality);
			} catch (MalformedURLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				//aqiText.setText("网络错误");
				e.printStackTrace();
			} 
	    }  
	};  

	
	private void button() {
		//计时器和跳转按钮
		//默认暂停和停止不可用
		btn2.setEnabled(false);
		btn3.setEnabled(false);
		btn1.setOnClickListener(new View.OnClickListener() {//开始按钮			
			@Override
			public void onClick(View arg0) {
				timer.setBase(SystemClock.elapsedRealtime()-recordingTime); //跳过已经记录的时间，继续计时
				timer.start();
				btn1.setEnabled(false);
				btn2.setEnabled(true);
				btn3.setEnabled(true);//按下开始后仅暂停和停止可用			
			}
		});
		
		btn2.setOnClickListener(new View.OnClickListener() {//暂停按钮		
			@Override
			public void onClick(View arg0) {
				timer.stop();
				recordingTime = SystemClock.elapsedRealtime()- timer.getBase();//暂停时保存已经记录的时间，初始值为0
				btn1.setEnabled(true);
				btn2.setEnabled(false);
				btn3.setEnabled(true);//暂停时仅开始和暂停可用
			}
		});
		
		btn3.setOnClickListener(new View.OnClickListener() {//停止按钮			
			@Override
			public void onClick(View arg0) {
				recordingTime = 0;
				timer.setBase(SystemClock.elapsedRealtime());
				timer.stop();                                   //已记录时间清零，时间设0同时停止技术
				btn1.setEnabled(true);
				btn2.setEnabled(false);
				btn3.setEnabled(false);//停止时仅开始可用			
			}
		});
		
		btn4.setOnClickListener(new View.OnClickListener() {//跳转至轨迹
			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				startActivity(new Intent(MainActivity.this,LocationActivity.class));				
			}
		});
		
		btn5.setOnClickListener(new View.OnClickListener() {//跳转至路径规划			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				startActivity(new Intent(MainActivity.this,RoutePlanningActivity.class));
			}
		});
	}

}
