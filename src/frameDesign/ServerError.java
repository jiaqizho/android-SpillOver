package frameDesign;

public class ServerError extends Exception {
	
	private static final long serialVersionUID = 5507508657695381229L;
	
	public ServerError() {
        super();
    }
	public ServerError(String str){
		super(str);
	}
}
