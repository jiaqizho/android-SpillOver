package frameDesign;

public interface ResponseHandler {
	
	public void callErrorBack(Request<?> request);

	public void callBack(Request<?> request, Response response);
}
