package frameDesign;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import android.util.Log;

public class HttpLaunch implements HttpHeap {

	
	private static final int DEFULAT_TIMEOUT = 5000;
	
	@Override
	public BasicHttpResponse handlerRequest(Request<?> request) throws IOException {

		/* 2.2版本以下可能面临请求头加不上的问题哦？
		 * System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		 */
		URL url = new URL(request.getUrl());
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setConnectTimeout(DEFULAT_TIMEOUT);	
		if(request.getHeader() != null){
			for(Entry<String,String> entry : request.getHeader().entrySet()){
				connection.addRequestProperty(entry.getKey(), entry.getValue());					
			}
		}
		if(request.getEtag() != null){
			connection.addRequestProperty("If-None-Match",request.getEtag());
		} 
		if(request.getiMS() != null){
			connection.addRequestProperty("If-Modified-Since",request.getiMS());
		} 
		setPostParams(request,connection);
	 	ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            throw new IOException("连接失败");
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
		return response;
	}


	private void setPostParams(Request<?> request, HttpURLConnection connection) throws IOException {
		switch(request.method){
		case POST:
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			break;
		case GET:
			connection.setRequestMethod("GET");
			break;	
		}
        Map<String,String> map = request.getParam();
        if(map == null){
        	return;
        }
        String params = "";
        int record = 0;
        int size = map.entrySet().size();
        Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
        while(iterator.hasNext()){
        	record++;
        	Entry<String, String> entry = iterator.next();
        	if(record >= size){
        		params += entry.getKey() + "=" + entry.getValue();
        		break;
        	}
        	params += entry.getKey() + "=" + entry.getValue() + "&";
        }
        switch(request.method){
		case POST:
	        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
	        byte[] postBody = params.getBytes("UTF-8");
	        out.write(postBody);
	        out.close();
			break;
		case GET:
			request.reWriteUrl(request.getUrl() + "?" + params.substring(0, params.length()));
			Log.i("DemoLog", request.getUrl());
			break;	
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
