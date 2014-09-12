package frameDesign;

import java.util.Map;

public class Response {
	
	private byte[] datas;
	
	private Map<String,String> headers;

	public Response(byte[] datas, Map<String, String> headers) {
		this.datas = datas;
		this.headers = headers;
	}

	public byte[] getDatas() {
		return datas;
	}

	public void setDatas(byte[] datas) {
		this.datas = datas;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	
}
