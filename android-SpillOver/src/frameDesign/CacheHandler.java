package frameDesign;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import file.Cache;

public class CacheHandler extends Thread {
	
	private BlockingQueue<Request<?>> mQueue = null;
	
	private BlockingQueue<Request<?>> mNetQueue;
	
	private Cache mCache = null;

	private CacheJudgement mCacheJudge;
	
	private volatile boolean quit = false;
	
	
	public CacheHandler(BlockingQueue<Request<?>> mQueue,
			BlockingQueue<Request<?>> mNetQueue, Cache mCache) {
		this(mQueue,mNetQueue,mCache,new CacheJudgement());
	}

	public CacheHandler(BlockingQueue<Request<?>> mQueue, BlockingQueue<Request<?>> mNetQueue,
			Cache mCache,CacheJudgement judge) {
		this.mQueue = mQueue;
		this.mNetQueue = mNetQueue;
		this.mCache = mCache;
		this.mCacheJudge = judge;
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
					}
					if(mCacheJudge.hasTTl(entry.ttl) || mCacheJudge.hasExpired(entry.expires)){
						//回调处理
						continue;
					} 
						
					//过期了之后丢放etag 和  Last-Modified
					if(mCacheJudge.usefulEtag(entry.etag)) {
						request.setEtag(entry.etag);
					} else if(mCacheJudge.usefulIMS(entry.iMS)){
						request.setiMS(entry.iMS);
					} 
					
					
				} catch (IOException e) {
				} finally{
					if(quit){
						return;
					}
					mNetQueue.put(request);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
