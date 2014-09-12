package frameDesign;

public interface ResponseHandler {
	
	public void callErrorBack(Request<?> request);

	public void callBack(Request<?> request, byte[] responseContent,
			String callBackdata);
}
