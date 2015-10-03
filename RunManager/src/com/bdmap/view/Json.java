package com.bdmap.view;

import java.io.InputStream;

import java.net.URL;

import org.json.JSONObject;
import org.json.JSONArray;


public class Json {

		public static PM25 getWebJson(URL url) throws Exception
		{
			
			InputStream in= url.openStream();
			byte[] b = new byte[4196];
			in.read(b);
			String str=new String(b);//传回的b是JSON数组
			JSONArray jsonArray = new JSONArray(str);
			int iSize = jsonArray.length();//获取数组长度
			int i = iSize-1;//数组最后一个对象
			JSONObject json = jsonArray.getJSONObject(i);//取出最后一个对象的json
			
			
			PM25 pm=new PM25();
			pm.aqi = json.getString("aqi");
			pm.area = json.getString("area");
			pm.pm2_5 = json.getString("pm2_5");
			pm.pm2_5_24h = json.getString("pm2_5_24h");
			pm.position_name =json.getString("position_name");
			pm.primary_pollutant = json.getString("primary_pollutant");
			pm.quality = json.getString("quality");
			pm.station_code = json.getString("station_code");
			pm.time_point = json.getString("time_point");
			in.close();
			return pm;
		}

}
