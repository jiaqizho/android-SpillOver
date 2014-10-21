package frameDesign;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import file.Cache;

public class CacheHandler extends Thread {
	
	private BlockingQueue<Request<?>> mQueue = null;
	
	private BlockingQueue<Request<?>> mNetQueue;
	
	private Cache mCache = null;
	
	private CacheJudgement mCacheJudge;
	
	private ResponseParse mResponseParse = null;
	
	private ResponseHandler mCallBack = null;
	
	private volatile boolean isCancel = false;
	
	public boolean isCancel() {
		return isCancel;
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}

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
	 
	protected CacheHandler(Cache mCache,ResponseParse parse
			,CacheJudgement judge,ResponseHandler callBack){ 
		this.mCache = mCache;
		this.mCacheJudge = judge;
		this.mResponseParse = parse;
		this.mCallBack = callBack;
	}
	
	
	protected void setNotModifyHeader(Request<?> request ,Cache.Entry entry){
		if(mCacheJudge.usefulEtag(entry.etag)) {
			request.setEtag(entry.etag);
		} 
		if(mCacheJudge.usefulIMS(entry.iMS)){
			request.setiMS(entry.iMS);
		} 
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
						if(request.isForcedReload()){
							continue;
						}
						mNetQueue.put(request);
						continue;
					}
					
					if(mCacheJudge.hasTTl(entry.ttl) || mCacheJudge.hasExpired(entry.expires)
							|| request.isForcedReload()){ 
						String callBackdata = null;
			        	callBackdata = mResponseParse.byteToEntity(entry.datas,entry.headers);
			        	mCallBack.callBack(request, new Response(entry.datas, callBackdata));
						continue;
					} 
					//过期了之后丢放etag 和  Last-Modified
					setNotModifyHeader(request,entry);
					mNetQueue.put(request);
				} catch (IOException e) {
					mNetQueue.put(request);
				} 
				
			} catch (InterruptedException e) {
				if(isCancel){
					Thread.currentThread().interrupt();
				}
				this.start();
			}
		}
	}
	
}
