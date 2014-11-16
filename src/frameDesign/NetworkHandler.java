package frameDesign;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;

import android.util.Log;

import file.Cache;
import file.IndexPoolOverflowException;

public class NetworkHandler extends Thread{

	private HttpHeap mHttpHeap = null;
	
	private BlockingQueue<Request<?>> mQueue = null;

	private Cache mCache = null;
	
	private ResponseParse mResponseParse = null;

	protected static final int DEFAULT_POOL_SIZE = 4096;
	
	private ResponseHandler mCallBack = null;
	
	private volatile boolean isCancel = false;
	
	public boolean isCancel() {
		return isCancel;
	}
	
	private Object cacheLock = new Object();

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}

	/**
	 * 外部接口
	 * @param mQueue
	 * @param mCache
	 * @param mHttpHeap
	 * @param parse
	 * @param response
	 */
	public NetworkHandler(BlockingQueue<Request<?>> mQueue, Cache mCache,
			HttpHeap mHttpHeap , ResponseParse parse , ResponseHandler response) {
		this.mQueue = mQueue;
		this.mCache = mCache;
		this.mHttpHeap = mHttpHeap;
		this.mResponseParse = parse;
		this.mCallBack = response;
	}
	
	/**
	 * 同包的接口,使得同胞可以用NetworkHandler来处理有关请求缓存的事情;
	 * @param mCache
	 * @param mHttpHeap
	 * @param parse
	 * @param response
	 */
	protected NetworkHandler(Cache mCache,
			HttpHeap mHttpHeap , ResponseParse parse , ResponseHandler response){
		this.mCache = mCache;
		this.mHttpHeap = mHttpHeap;
		this.mResponseParse = parse;
		this.mCallBack = response;
	}
	
	/**
	 * 同包接口,处理304情况,处理好了直接回调
	 * 
	 * @param request
	 * @throws IOException 
	 */
	protected void noModifiedHandler(Request<?> request,Map<String,String> responseHeaders) throws IOException{
		Cache.Entry entry = mCache.get(request.getUrl());
		callBackResult(request,entry.datas,responseHeaders);
	}

	/**
	 * 同包接口,处理ttl之外的缓存过程,因为ttl会涉及外部的处理,所以只能把他分出来
	 * 
	 * @param requestKey
	 * @param entry
	 * @param responseHeaders
	 * @param responseContent
	 * @throws IOException
	 * @throws IndexPoolOverflowException
	 */
    protected void cacheWithoutTTL(String requestKey,Cache.Entry entry, Map<String, String> responseHeaders,
    		byte[] responseContent) throws IOException, IndexPoolOverflowException {
		entry.expires = mResponseParse.parseExpires(responseHeaders.get("Expires"), responseHeaders.get("Date"));
		entry.iMS = responseHeaders.get("Last-Modified");
		entry.etag = responseHeaders.get("ETag");
		entry.headers = responseHeaders;
		entry.datas = responseContent;
		synchronized(cacheLock){	//防止正在cache的时候进行的cache.get操作
			mCache.put(requestKey, entry);
		}
	}
	
    protected void callBackResult(Request<?> request , byte[] responseContent , Map<String,String> responseHeaders){
		String callBackdata = mResponseParse.byteToEntity(responseContent,responseHeaders);
		Response response = new Response(responseContent, callBackdata);
    	mCallBack.callBack(request, response);    	
    }
    
	@Override
	public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		while(true){
			Request<?> request = null;
			try {
				request = mQueue.take();
				BasicHttpResponse response = mHttpHeap.handlerRequest(request);
				if(response == null){
					mCallBack.callErrorBack(request);
					continue;
				}
				byte[] responseContent = mResponseParse.entityToBytes(
						response.getEntity(), new ByteArrayPool(DEFAULT_POOL_SIZE));
				Map<String,String> responseHeaders = convertHeaders(response.getAllHeaders());
		        StatusLine statusLine = response.getStatusLine();
		        int statusCode = statusLine.getStatusCode();
		        
				//304操作;
		        if(statusCode == HttpStatus.SC_NOT_MODIFIED){
		        	noModifiedHandler(request,responseHeaders);
		        	continue;
				}

		        if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
		        
		        // 设好缓存 
				if(request.shouldCache()){
					Cache.Entry entry = new Cache.Entry();
					long ttl = mResponseParse.parseTtl(responseHeaders.get("Cache-Control"));
					if(ttl == -1){
						callBackResult(request,responseContent,responseHeaders);
						continue;
					} 
					entry.ttl = ttl;
					cacheWithoutTTL(request.getUrl(),entry,responseHeaders,responseContent);
				}
				callBackResult(request,responseContent,responseHeaders);
			} catch (IOException e) {
				if(request != null){
					mCallBack.callErrorBack(request);
				}
			} catch (InterruptedException e) {
				if(isCancel){
					Thread.currentThread().interrupt();
				} 
				this.start();
			} catch (ServerError e) {
				e.printStackTrace();
			} catch (IndexPoolOverflowException e) {
				e.printStackTrace();
			} 
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
