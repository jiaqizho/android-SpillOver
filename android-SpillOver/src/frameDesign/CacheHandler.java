package frameDesign;

import java.util.concurrent.BlockingQueue;

import file.Cache;

public class CacheHandler extends Thread {
	
	private BlockingQueue<Request<?>> mQueue = null;
	
	private Cache mCache = null;

	public CacheHandler(BlockingQueue<Request<?>> mQueue, Cache mCache) {
		this.mQueue = mQueue;
		this.mCache = mCache;
	}

	@Override
	public void run() {
		while(true){
			try {
				Request<?> request = mQueue.take();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
