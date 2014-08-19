package frameDesign;

import java.io.IOException;

import org.apache.http.message.BasicHttpResponse;

public interface HttpHeap {
	public BasicHttpResponse handlerRequest(Request<?> request) throws IOException;
}
