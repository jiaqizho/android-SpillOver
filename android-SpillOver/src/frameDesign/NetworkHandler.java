package frameDesign;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import android.util.Log;
import file.Cache;

public class NetworkHandler extends Thread{

	private HttpHeap mHttpHeap = null;
	
	private BlockingQueue<Request<?>> mQueue = null;

	private Cache mCache = null;

	public NetworkHandler(BlockingQueue<Request<?>> mQueue, Cache mCache, HttpHeap mHttpHeap) {
		this.mQueue = mQueue;
		this.mCache = mCache;
		this.mHttpHeap = mHttpHeap;
	}

	@Override
	public void run() {
		while(true){
			try {
				Request<?> request = mQueue.take();
				//System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
				URL url = new URL(request.getUrl());
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				for(Entry<String,String> entry : request.getHeader().entrySet()){
					connection.setRequestProperty(entry.getKey(), entry.getValue());					
				}
			 	ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
		        int responseCode = connection.getResponseCode();
		        if (responseCode == -1) {
		            throw new IOException("¡¨Ω” ß∞‹");
		        }
		        StatusLine responseStatus = new BasicStatusLine(protocolVersion,
		                connection.getResponseCode(), connection.getResponseMessage());
		        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
		        response.setEntity(entityFromConnection(connection));
		        for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
		            if (header.getKey() != null) {
		                Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
		                response.addHeader(h);
		            }
		        }
		        
		        for(int i = 0 ; i < response.getAllHeaders().length ; i++)
		        	Log.i("VolleyPatterns", response.getAllHeaders()[i] + "");
				
				
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private HttpEntity entityFromConnection(HttpURLConnection connection) {
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
	}
	
}
