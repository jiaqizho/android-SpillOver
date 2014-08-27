package frameDesign;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import android.util.Log;
import file.Cache;

public class CacheHandler extends Thread {
	
	private BlockingQueue<Request<?>> mQueue = null;
	
	private BlockingQueue<Request<?>> mNetQueue;
	
	private Cache mCache = null;

	private CacheJudgement mCacheJudge;
	
	private volatile boolean quit = false;

	private ResponseParse mResponseParse = null;
	
	private ResponseHandler mCallBack = null;
	
	public CacheHandler(BlockingQueue<Request<?>> mQueue,
			BlockingQueue<Request<?>> mNetQueue, Cache mCache,ResponseParse parse,ResponseHandler callBack) {
		this(mQueue,mNetQueue,mCache,parse,new CacheJudgement(),callBack);
	}
	
	public CacheHandler(BlockingQueue<Request<?>> mQueue, BlockingQueue<Request<?>> mNetQueue,
			Cache mCache,ResponseParse parse,CacheJudgement judge,ResponseHandler callBack) {
		this.mQueue = mQueue;
		this.mNetQueue = mNetQueue;
		this.mCache = mCache;
		this.mCacheJudge = judge;
		this.mResponseParse = parse;
		this.mCallBack = callBack;
	} 
	
	
	@Override
	public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		if(mCache.initialize()){
			//设置cache队列的取消
		}
		while(true){
			try {
				Request<?> request = mQueue.take();
				try { 
					Cache.Entry entry = mCache.get(request.getUrl());
					if(entry == null){
						mNetQueue.put(request);
						continue;
					}
					
					if(mCacheJudge.hasTTl(entry.ttl) || mCacheJudge.hasExpired(entry.expires)){ 
						String callBackdata = null;
			        	callBackdata = mResponseParse.byteToEntity(entry.datas,entry.headers);
			        	mCallBack.callBack(request, callBackdata);
						continue;
					} 
					
					//过期了之后丢放etag 和  Last-Modified
					if(mCacheJudge.usefulEtag(entry.etag)) {
						request.setEtag(entry.etag);
					} else if(mCacheJudge.usefulIMS(entry.iMS)){
						request.setiMS(entry.iMS);
					} 
					
					mNetQueue.put(request);
				} catch (IOException e) {
					Log.i("DemoLog", 3+"");
					mNetQueue.put(request);
				} finally{
					if(quit){
						return;
					}
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
