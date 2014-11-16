package frameDesign;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

/**
 * Thread Safe 无竞态
 */
public class HttpResponseParse implements ResponseParse{

	
	/**
	 * 解析GMT时间
	 * @param dateStr
	 * @return
	 */
    private long parseDateAsEpoch(String dateStr) {
        try {
            return DateUtils.parseDate(dateStr).getTime() ;
        } catch (DateParseException e) {
            return 0;
        }
    }

    /**
     * @return 
     * 		当返回 -1 所有缓存失效
     */
	@Override
	public long parseTtl(String headerValue) {
		
		long now = System.currentTimeMillis(); 
        long maxAge = 0;
        
        if (headerValue != null) {
            String[] tokens = headerValue.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.equals("no-cache") || token.equals("no-store")) {
                    return -1;
                } else if (token.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception e) {
                    }
                } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                    maxAge = 0;
                }
            } 
        }
        return (now / 1000 + maxAge);
	}

	@Override
	public long parseExpires(String headerValue,String headerServerDate) {
		long now = System.currentTimeMillis() ;
		
        long serverExpires = 0;
        if (headerValue != null) {
            serverExpires = parseDateAsEpoch(headerValue);
        } 
        
        long serverDate = 0;
        if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerServerDate);
        }
        if (serverDate > 0 && serverExpires >= serverDate) {
        	return (now + (serverExpires - serverDate)) / 1000;
        } 
        
		return 0;
	}
	
	public static String parseCharset(Map<String, String> headers) {
        String contentType = headers.get(HTTP.CONTENT_TYPE);
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }

        return HTTP.DEFAULT_CONTENT_CHARSET;
    }
	
	
	public byte[] entityToBytes(HttpEntity entity , ByteArrayPool mPool) throws IOException, ServerError {
        PoolingByteArrayOutputStream bytes =
                new PoolingByteArrayOutputStream(mPool, (int) entity.getContentLength());
        byte[] buffer = null;
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new ServerError();
            }
            buffer = mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            try {
                entity.consumeContent();
            } catch (IOException e) {
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }
	
	public String byteToEntity(byte[] data,Map<String,String> headers){
		 String parsed;
		 try {
			 parsed = new String(data, parseCharset(headers));
		 } catch (UnsupportedEncodingException e) {
			 parsed = new String(data);
		 }
		 return parsed;
	}
}
