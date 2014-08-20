package frameDesign;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import android.util.Log;

import file.Cache;

public class RequestHandler implements Handler {

	private HttpHeap mHttpHeap;
	
	private Cache mCache;
	
	private BlockingQueue<Request<?>> mCacheQueue = null;
	
	private BlockingQueue<Request<?>> mNetQueue = null;
	
	private CacheHandler mCacheHandler = null;
	
	private NetworkHandler mNetworkHandler = null;
	
	private ResponseParse parse;
	
	public RequestHandler(HttpHeap heap, Cache cache) {
		this(heap,cache,new HttpResponseParse());
	}

	
	public RequestHandler(HttpHeap heap, Cache cache,ResponseParse parse) {
		this.mHttpHeap = heap;
		this.mCache = cache;
		mCacheQueue = new PriorityBlockingQueue<Request<?>>();
		mNetQueue = new PriorityBlockingQueue<Request<?>>();
		this.parse = parse;
	}

	
	@Override
	public void parseRequest(Request<?> request) {
		
	}

	@Override
	public void init()  {
		mCacheHandler = new CacheHandler(mCacheQueue, mNetQueue,mCache);
		mNetworkHandler = new NetworkHandler(mNetQueue,mCache,mHttpHeap,parse);
		mCacheHandler.start();
		mNetworkHandler.start(); 
		mNetQueue.add(new Request<String>("http://192.168.1.104:8080/QQServer/Expires",new Request.ResponseListener<String>() {

			@Override
			public void callBack(String arg0) {
				Log.i("DemoLog", Thread.currentThread().toString() + "");
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
	}
	
}
