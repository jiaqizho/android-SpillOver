package frameDesign;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import android.os.Looper;
import file.Cache;

public class RequestHandler  {

	private HttpHeap mHttpHeap;
	
	private Cache mCache;
	
	private BlockingQueue<Request<?>> mCacheQueue = null;
	
	private BlockingQueue<Request<?>> mNetQueue = null;
	
	private CacheHandler mCacheHandler = null;
	
	private NetworkHandler mNetworkHandler = null;
	
	private ResponseParse parse;
	
	private ResponseHandler mCallBack = null;
	
	private RequestMemoizer mRequestMemoizer = null;
	
	public RequestHandler(HttpHeap heap, Cache cache) {
		this(heap,cache,new HttpResponseParse(),new CallBackResponse(new android.os.Handler(Looper.getMainLooper())));
	}
	
	public RequestHandler(HttpHeap heap, Cache cache,ResponseParse parse,ResponseHandler callBack) {
		this.mHttpHeap = heap;
		this.mCache = cache;
		mCacheQueue = new PriorityBlockingQueue<Request<?>>();
		mNetQueue = new PriorityBlockingQueue<Request<?>>();
		this.parse = parse;
		this.mCallBack = callBack;
	}

	public void init()  {
		mCacheHandler = new CacheHandler(mCacheQueue, mNetQueue,mCache,parse,mCallBack);
		mNetworkHandler = new NetworkHandler(mNetQueue,mCache,mHttpHeap,parse,mCallBack);
		mCacheHandler.start();
		mNetworkHandler.start(); 
		mRequestMemoizer = new RequestMemoizer(mCache, parse, mHttpHeap, mCallBack);
	}

	public void add(Request<?> request){
		if(request.shouldCache()){
			mCacheQueue.add(request);
		} else {
			mNetQueue.add(request);
		}
	}
	
	public void addInMemoizer(Request<?> request){
		mRequestMemoizer.add(request);
	}
}
