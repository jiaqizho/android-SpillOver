package frameDesign;

import java.util.Map;

public class Response {
	
	private byte[] datas;
	
	private String callBackdata;

	public Response(byte[] datas, String callBackdata) {
		this.datas = datas;
		this.callBackdata = callBackdata;
	}

	public byte[] getDatas() {
		return datas;
	}

	public void setDatas(byte[] datas) {
		this.datas = datas;
	}

	public String getCallBackdata() {
		return callBackdata;
	}

	public void setCallBackdata(String callBackdata) {
		this.callBackdata = callBackdata;
	}
	
}
