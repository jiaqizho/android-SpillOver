package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;

import com.example.android_spillover.R;

import file.IndexPoolOverflowException;
import frameDesign.Request;
import frameDesign.SpillOver;

public class MainActivity extends Activity{

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainview);

		
		new Thread(new Runnable() { 
			
			@Override
			public void run() {
				try {
					SpillOver.newRequestQueue(MainActivity.this).add(new Request<String>("http://192.168.1.104:8080/QQServer/Expires",new Request.ResponseListener<String>() {

							@Override
							public void callBack(String arg0) {
								//Log.i("DemoLog", "Data:" + arg0);
							}
			
					}) {
			
						@Override
						public boolean shouldCache() {
							return true;
						}

						@Override
						protected String handlerCallBack() { 
							return "dddd";
						}
			
						@Override
						public Map<String, String> getHeader() {
							Map<String,String> map = new HashMap<String, String>();
							map.put("User-Agent", "nimashabi");
							return map;
						}
			
						@Override
						public Map<String, String> getParam() {
							Map<String,String> map = new HashMap<String, String>();
							map.put("qusiba", "jilao");
							return map;
						}
						
					});
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (IndexPoolOverflowException e1) {
					e1.printStackTrace();
				}
			}
		}).start();
	}
	
}
