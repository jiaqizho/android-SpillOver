package test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
		
			URL url = new URL("http://192.168.1.104:8080/QQServer/Expires");
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			setPostParams(connection);
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

	private void setPostParams(HttpURLConnection connection) throws IOException {
        connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.getOutputStream();
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        String s = "aaba=dddd";
        byte[] postBody = s.getBytes("UTF-8");
        out.write(postBody);
        out.close();
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
