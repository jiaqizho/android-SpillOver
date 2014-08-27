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

public class HttpLaunch implements HttpHeap {

	@Override
	public BasicHttpResponse handlerRequest(Request<?> request) throws IOException {

		//System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		URL url = new URL(request.getUrl());
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		if(request.getHeader() != null){
			for(Entry<String,String> entry : request.getHeader().entrySet()){
				connection.addRequestProperty(entry.getKey(), entry.getValue());					
			}
			if(request.getEtag() != null){
				connection.addRequestProperty("If-None-Match",request.getEtag());
			} 
			if(request.getiMS() != null){
				connection.addRequestProperty("If-Modified-Since",request.getEtag());
			} 
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
		connection.setDoOutput(true);
		switch(request.method){
		case POST:
			connection.setRequestMethod("POST");
			break;
		case GET:
			connection.setRequestMethod("GET");
			break;	
		}
		connection.getOutputStream();
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        Map<String,String> map = request.getParam();
        if(map == null){
        	return;
        }
        String params = null;
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
