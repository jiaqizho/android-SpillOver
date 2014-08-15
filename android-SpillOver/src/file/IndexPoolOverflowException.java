package file;

public class IndexPoolOverflowException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IndexPoolOverflowException(){
		super("IndexPoolOverflow");
	}
	
	public IndexPoolOverflowException(String str){
		super(str);
	}
}
