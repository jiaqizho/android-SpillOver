package frameDesign;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Build;
import file.BasicCalculator;
import file.BasicFileCache;
import file.Cache;
import file.IndexPoolOverflowException;

public class SpillOver {
	
	public static final String DEFAULT_CACHE_DIR = "spillover";
	
	private static boolean isAlive = false;
	
	public static Handler newRequestQueue(Context context) throws IOException, IndexPoolOverflowException{
		
		synchronized(SpillOver.class){
			if(!isAlive){
				isAlive = true;
			} else {
				return null;
			}
		}
		
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR); 
		Cache cache = new BasicFileCache(new BasicCalculator(),cacheDir);
		
		HttpHeap heap = null;
		if(Build.VERSION.SDK_INT >= 9 ){
			heap = new HttpLaunch();
		} else {
			throw new Error(new Throwable("SDK版本出现问题"));
		}
		Handler handler = new RequestHandler(heap,cache);
		handler.init();
		
		return handler;
	}
	

}