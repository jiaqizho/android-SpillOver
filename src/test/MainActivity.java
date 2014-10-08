package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;

import com.example.android_spillover.R;

import file.IndexPoolOverflowException;
import frameDesign.Request;
import frameDesign.SpillOver;

public class MainActivity extends Activity{

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainview);
	}
}
