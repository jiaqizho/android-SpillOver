package test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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


public class test {
	public void testd() throws Exception {
		
			URL url = new URL("http://www.baidu.com");
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		 	ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
	        int responseCode = connection.getResponseCode();
	        if (responseCode == -1) {
	            // -1 is returned by getResponseCode() if the response code could not be retrieved.
	            // Signal to the caller that something was wrong with the connection.
	            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
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
	}
	
    private static HttpEntity entityFromConnection(HttpURLConnection connection) {
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
