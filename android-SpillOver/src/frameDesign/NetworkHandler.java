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

	private static final int DEFAULT_POOL_SIZE = 4096;
	
	public NetworkHandler(BlockingQueue<Request<?>> mQueue, Cache mCache, HttpHeap mHttpHeap , ResponseParse parse) {
		this.mQueue = mQueue;
		this.mCache = mCache;
		this.mHttpHeap = mHttpHeap;
		this.mResponseParse = parse;
	}
	
	@Override
	public void run() {
	
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		
		while(true){
			try {
				Request<?> request = mQueue.take();
				BasicHttpResponse response = mHttpHeap.handlerRequest(request);
				Map<String,String> responseHeaders = convertHeaders(response.getAllHeaders());
		        StatusLine statusLine = response.getStatusLine();
		        int statusCode = statusLine.getStatusCode();
		        if(statusCode == HttpStatus.SC_NOT_MODIFIED){
		        	
		        	
					//304操作; 
				}
		        
		        /**
		         * 设好缓存
		         */
				if(request.shouldCache()){
					Cache.Entry entry = new Cache.Entry();
					long ttl = mResponseParse.parseTtl(responseHeaders.get("Cache-Control"));
					if(ttl == -1){
						continue; 
					} 
					entry.ttl = ttl;
					entry.expires = mResponseParse.parseExpires(responseHeaders.get("Expires"), responseHeaders.get("Date"));
					entry.iMS = responseHeaders.get("Last-Modified");
					entry.etag = responseHeaders.get("Etag");
					entry.headers = responseHeaders;
					entry.datas = mResponseParse.entityToBytes(response.getEntity(), new ByteArrayPool(DEFAULT_POOL_SIZE));
					mCache.put(request.getUrl(), entry);
				}
				
				
				
				/**
				 * 处理完缓存之后就进行reponse回调操作
				 */
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
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
