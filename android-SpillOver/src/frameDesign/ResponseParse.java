package frameDesign;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;

public interface ResponseParse {
	
	public long parseTtl(String ttl);
	
	public long parseExpires(String expires,String serverDate);
	
	public byte[] entityToBytes(HttpEntity entity , ByteArrayPool mPool) throws IOException, ServerError ;

	public String byteToEntity(byte[] bd, Map<String, String> responseHeaders);
}
