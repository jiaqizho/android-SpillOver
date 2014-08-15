package frameDesign;

import java.util.Map;

public abstract class Request <T> implements Comparable<Request<T>> {
	
	public Request(String url){
		this.mUrl = url;
	}
	
	private enum Priority{
		LOW,
		MEDIUM,
		HIGH
	}
	
	public Priority getPriority(){
		return Priority.MEDIUM; 
	}
	
	public boolean shouldCache(){
		return true;
	}
	
	private String mUrl;
	
	public String getUrl(){
		return mUrl;
	}
	
	protected Map<String,String> headers;	//请求头
	
	protected Map<String,String> params;	//请求参数
	
	public abstract Map<String,String> getHeader();	//回调请求头
	
	public abstract Map<String,String> getParam(); 	//回调参数列表

	protected abstract T handlerCallBack();
	
	@Override
	public int compareTo(Request<T> another) {
		
		if(this.getPriority() == another.getPriority()){
			return 0;
		}
		
		return another.getPriority().ordinal() - this.getPriority().ordinal();
	}
	
	
}
