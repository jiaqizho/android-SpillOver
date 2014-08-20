package test;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;

import com.example.android_spillover.R;

import file.IndexPoolOverflowException;
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
					SpillOver.newRequestQueue(MainActivity.this);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (IndexPoolOverflowException e1) {
					e1.printStackTrace();
				}
			}
		}).start();
	}
	
}
