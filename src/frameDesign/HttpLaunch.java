package frameDesign;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

import frameDesign.Request.Method;

import android.util.Log;

public class HttpLaunch implements HttpHeap {

	
	private static final int DEFULAT_TIMEOUT = 5000;
	
	private String reBackRequest;
	
	@Override
	public BasicHttpResponse handlerRequest(Request<?> request) throws IOException {

		/* 2.2版本以下可能面临请求头加不上的问题哦？
		 * System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		 */
		
		reBackRequest = request.getUrl();
		if(request.method == Method.GET){
			setGetParams(request);
		}
		
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
		
		if(request.method == Method.POST){
			setPostParams(request,connection);
		}
		
		ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        int responseCode = -1;
		try {
			responseCode = connection.getResponseCode();
		} catch (IOException e) {
			throw new IOException();
		} finally{
			request.reWriteUrl(reBackRequest);
		}
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

	
	private void setGetParams(Request<?> request){
		String params = getParams(request);
		if(params != null && !params.equals("")){
			request.reWriteUrl(request.getUrl() + "?" + params.substring(0, params.length()));
		}
	}

	private String getParams(Request<?> request){
		Map<String,String> map = request.getParam();
        if(map == null){
        	return null;
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
        return params;
	}
	
	private void setPostParams(Request<?> request, HttpURLConnection connection) throws IOException {
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		String params = getParams(request);
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		byte[] postBody = params.getBytes("UTF-8");
		out.write(postBody);
		out.close();
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
