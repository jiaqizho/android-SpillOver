/*package frameDesign;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;

import android.content.Context;
import android.os.Looper;
import file.BasicCalculator;
import file.BasicFileCache;
import file.Cache;
import file.IndexPoolOverflowException;

public class ConcurrentHandler {
	
	private static final int DEFAULT_CONTORL_NUM = 5;
	
	private int ContorlNum = DEFAULT_CONTORL_NUM;
	
	private Semaphore mSemaphore;
	
	public int getContorlNum() {
		return ContorlNum;
	}

	public void setContorlNum(int contorlNum) {
		ContorlNum = contorlNum;
	}

	private Cache mCache;
	
	private HttpHeap mHttpHeap;
	
	private ResponseHandler mCallBack;
	
	private CacheJudgement mCacheJudge;
	
	private ResponseParse mResponseParse;
	
	private CacheHandler mCacheHandler;
	
	private NetworkHandler mNetworkHandler;
	
	protected ExecutorService service;
	
	public void shutDown() {
		if(!service.isShutdown()){
			service.shutdown();
		}
	}
	
	public void shutdownNow(){
		if(!service.isShutdown()){
			service.shutdownNow();
		}
	}
	
	public ConcurrentHandler(Cache mCache, HttpHeap mHttpHeap,
			ResponseHandler mCallBack, CacheJudgement mCacheJudge,
			ResponseParse mResponseParse) {
		this.mCache = mCache;
		this.mHttpHeap = mHttpHeap;
		this.mCallBack = mCallBack;
		this.mCacheJudge = mCacheJudge;
		this.mResponseParse = mResponseParse;
		this.mCacheHandler = new CacheHandler(mCache, mResponseParse, mCacheJudge, mCallBack);
		this.mNetworkHandler = new NetworkHandler(mCache, mHttpHeap, mResponseParse, mCallBack);
		service = Executors.newCachedThreadPool();
		mSemaphore = new Semaphore(ContorlNum);
	}
	
	public static final String DEFAULT_CACHE_DIR = "spillover";
	
	
	*//**
	 * 为外部提供的接口
	 * @param context
	 *//*
	public ConcurrentHandler(Context context){
		this(new BasicFileCache(new BasicCalculator(),new File(context.getCacheDir(), DEFAULT_CACHE_DIR)),new HttpLaunch()
			,new CallBackResponse(new android.os.Handler(Looper.getMainLooper()))
			,new CacheJudgement(),new HttpResponseParse());
	}
	
	*//**
	 * 为RequestHandler提供的接口
	 * @param mCache
	 * @param mHttpHeap
	 * @param mCallBack
	 * @param parse
	 *//*
	public ConcurrentHandler(Cache mCache, HttpHeap mHttpHeap,
			ResponseHandler mCallBack, ResponseParse parse) {
		this(mCache,mHttpHeap,mCallBack,new CacheJudgement(),parse);
	}

	*//**
	 *	外部请求用for一个整体循环
	 * 
	 * @param request
	 * @throws InterruptedException 
	 *//*
	public void add(final Request<?> request) {
		service.execute(new Runnable() {
			@Override 
			public void run() {
				try { 
					mSemaphore.acquire();
					Cache.Entry entry = mCache.get(request.getUrl());
					if(entry == null){
						noReqCacheRequest(request);
						return;
					}
					if(mCacheJudge.hasTTl(entry.ttl) || mCacheJudge.hasExpired(entry.expires)){ 
						String callBackdata = null;
			        	callBackdata = mResponseParse.byteToEntity(entry.datas,entry.headers);
			        	mCallBack.callBack(request, new Response(entry.datas, callBackdata));
			        	return;
					}
					 
					mCacheHandler.setNotModifyHeader(request, entry);
					noReqCacheRequest(request);
				} catch (IOException e) {
					noReqCacheRequest(request);
				} catch (InterruptedException e) {
					if(Thread.currentThread().isInterrupted()){
						Thread.currentThread().interrupt();
						service.shutdown();
						return;
					}
				} finally {
					releaseThreadSemaphore();
				}
				
			}
		});
	}

	
	protected void noReqCacheRequest(Request<?> request){
		try{
			BasicHttpResponse response = mHttpHeap.handlerRequest(request);
			if(response == null){
				releaseThreadSemaphore();
				mCallBack.callErrorBack(request);
				return;
			}
			byte[] responseContent = mResponseParse.entityToBytes(
					response.getEntity(), new ByteArrayPool(mNetworkHandler.DEFAULT_POOL_SIZE));
			Map<String,String> responseHeaders = convertHeaders(response.getAllHeaders());
	        StatusLine statusLine = response.getStatusLine();
	        int statusCode = statusLine.getStatusCode();
	        
			//304操作;
	        if(statusCode == HttpStatus.SC_NOT_MODIFIED){
	        	mNetworkHandler.noModifiedHandler(request,responseHeaders);
	        	return;
			}
	        
	        // 设好缓存 
			if(request.shouldCache()){
				Cache.Entry entry = new Cache.Entry();
				long ttl = mResponseParse.parseTtl(responseHeaders.get("Cache-Control"));
				if(ttl == -1){
					mNetworkHandler.callBackResult(request, responseContent, responseHeaders);
					return; 
				} 
				entry.ttl = ttl;
				mNetworkHandler.cacheWithoutTTL(request.getUrl(),entry,responseHeaders,responseContent);
			}
			mNetworkHandler.callBackResult(request, responseContent, responseHeaders);
		} catch (IOException e) { 
			mCallBack.callErrorBack(request);
		} catch (ServerError e) {
			e.printStackTrace();
		} catch (IndexPoolOverflowException e) {
			e.printStackTrace();
		}
	}

	private void releaseThreadSemaphore(){
		if(!Thread.currentThread().isInterrupted()){
			mSemaphore.release();
		}
	}
	
    private static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
    
    
}*/