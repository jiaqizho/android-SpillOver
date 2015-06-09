package frameDesign;

public interface Handler {
	
	public void parseRequest(Request<?> request);

	public void init();
	
}
