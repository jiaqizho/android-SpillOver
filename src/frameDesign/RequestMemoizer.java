package frameDesign;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;

import android.util.Log;

import file.Cache;
import file.IndexPoolOverflowException;

/**
 * @author user
 * 用来实现请求比对
 * @param <A>
 * @param <V>
 */
public class RequestMemoizer {

	private ConcurrentMap<Request<?>,Future<Response>> cache = 
			new ConcurrentHashMap<Request<?>,Future<Response>>();

	public RequestMemoizer(Cache mCache, CacheJudgement mCacheJudge,
			ResponseParse mResponseParse, HttpHeap mHttpHeap,
			ResponseHandler mCallBack) {
		this.mCache = mCache;
		this.mCacheJudge = mCacheJudge;
		this.mResponseParse = mResponseParse;
		this.mHttpHeap = mHttpHeap;
		this.mCallBack = mCallBack;
	}

	public RequestMemoizer(Cache mCache, ResponseParse parse,
			HttpHeap mHttpHeap, ResponseHandler mCallBack) {
		this(mCache, new CacheJudgement(),parse, mHttpHeap, mCallBack);
	}

	private Cache mCache = null;
	
	private CacheJudgement mCacheJudge = null;
	
	private ResponseParse mResponseParse = null;
	
	private HttpHeap mHttpHeap = null;
	
	protected static final int DEFAULT_POOL_SIZE = 4096;

	private ResponseHandler mCallBack = null;
	
	public void add(Request<?> request){
		Response response = null;
		try {
			response = compute(request);
		} catch (InterruptedException e) {
			//future等待的时候抛出的打断异常
		}
		if(response != null){
			mCallBack.callBack(request, response);
		} else {
			mCallBack.callErrorBack(request);
		}
	}
	
	private Response compute(final Request<?> request) throws InterruptedException {
		Future<Response> future = cache.get(request);
		if(future == null){
			Callable<Response> eval = new Callable<Response>() {
 
				@Override
				public Response call() {
					try {
						Cache.Entry entry = mCache.get(request.getUrl());
						if(entry != null){
							if(mCacheJudge.hasTTl(entry.ttl) || mCacheJudge.hasExpired(entry.expires)){
					        	return new Response(entry.datas, mResponseParse.byteToEntity(entry.datas,entry.headers));
							} else {
								setNotModifyHeader(request,entry);
							}
						}
						BasicHttpResponse response = mHttpHeap.handlerRequest(request);
						if(response == null){
							return null;
						}
						byte[] responseContent = mResponseParse.entityToBytes(
								response.getEntity(), new ByteArrayPool(DEFAULT_POOL_SIZE));
						Map<String,String> responseHeaders = convertHeaders(response.getAllHeaders());
				        StatusLine statusLine = response.getStatusLine();
				        int statusCode = statusLine.getStatusCode();
				        
						//304操作;
				        if(statusCode == HttpStatus.SC_NOT_MODIFIED){
				        	String callBackdata = mResponseParse.byteToEntity(entry.datas,entry.headers);
				        	return new Response(entry.datas, callBackdata);
						}
				        
				        // 设好缓存 
						if(request.shouldCache()){
							Cache.Entry entryC = new Cache.Entry();
							long ttl = mResponseParse.parseTtl(responseHeaders.get("Cache-Control"));
							if(ttl != -1){
								entryC.ttl = ttl;
								entryC.expires = mResponseParse.parseExpires(responseHeaders.get("Expires"), responseHeaders.get("Date"));
								entryC.iMS = responseHeaders.get("Last-Modified");
								entryC.etag = responseHeaders.get("ETag");
								entryC.headers = responseHeaders;
								entryC.datas = responseContent;
								mCache.put(request.getUrl(), entryC); 		
							} 
						}
						String callBackdata = mResponseParse.byteToEntity(responseContent,responseHeaders);
						return new Response(responseContent, callBackdata);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (IndexPoolOverflowException e) {
						Log.i("DemoLog2", "fufufufuf");
						mCache.delete(request.getUrl());
						return call();
					} catch (ServerError e) {
						e.printStackTrace();
					}
					return null;
					
				}
			};
			final FutureTask<Response> task = new FutureTask<Response>(eval);
			future = task; 
			cache.put(request, task);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					task.run();
				}
			}).start();
		} else {
			Log.i("DemoLog", "2");
		}
		try {
			return future.get();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected void setNotModifyHeader(Request<?> request ,Cache.Entry entry){
		if(mCacheJudge.usefulEtag(entry.etag)) {
			request.setEtag(entry.etag);
		} 
		if(mCacheJudge.usefulIMS(entry.iMS)){
			request.setiMS(entry.iMS);
		} 
	}

	private static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
}
