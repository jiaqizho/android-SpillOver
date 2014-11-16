package frameDesign;

import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

/****
 * 1.能设置post get		默认post
 * 2.能设置request优先级		默认相同
 * 3.能设置是否进行缓存		默认缓存
 * 4.能设置请求头			默认null
 * 5.能设置参数头			默认null			
 * 6.强制读取缓存/不请求读缓存	默认false;
 * 
 * 
 */
public abstract class Request <T> implements Comparable<Request<T>>,Comparabler {
	
	public ResponseListener<T> listener;
	
	private String mUrl;
	
	private boolean forcedReload = false ; 	//强制读取缓存

	public Request(String url ,ResponseListener<T> listener){
		this.listener = listener;
		this.mUrl = url;
	}
	
	@Override
	public boolean compareWith(Comparabler compare) {
		Comparator comparator = new RequestComparator();
		return comparator.compare(this, compare);
	}

	public interface ResponseListener<T>{
		
		//这里的泛型是个坑。。。
		public void callBack(Object responseData) throws NullPointerException;
		
		public void callErrorBack(byte[] responseContent, String callBackdata) throws NullPointerException;
	}
	
	public Method method = Method.GET; 
	
	public static enum Method{
		GET,
		POST
	}
	private enum Priority{
		LOW,
		MEDIUM,
		HIGH
	}
	
	public Priority getPriority(){
		return Priority.MEDIUM; 
	}
	
	@Override
	public int compareTo(Request<T> another) {
		if(this.getPriority() == another.getPriority()){
			return 0;
		}
		return another.getPriority().ordinal() - this.getPriority().ordinal();
	}
	
	public boolean isForcedReload() {
		return forcedReload;
	}

	public void setForcedReload(boolean forcedReload) {
		this.forcedReload = forcedReload;
	}
	
	public boolean shouldCache(){
		return true;
	}
	
	public String getUrl(){
		return mUrl;
	}
	
	public void reWriteUrl(String str){
		this.mUrl = str;
	}
	
	private String Etag = null;
	
	public String getEtag() {
		return Etag;
	}

	public void setEtag(String etag) {
		Etag = etag;
	}
	
	private String iMS = null;
	
	public String getiMS() {
		return iMS;
	}

	public void setiMS(String iMS) {
		this.iMS = iMS;
	}

	protected Map<String,String> headers;	//请求头
	 
	protected Map<String,String> params;	//请求参数
	
	public abstract Map<String,String> getHeader();	//回调请求头
	
	public abstract Map<String,String> getParam(); 	//回调参数列表

	protected abstract T handlerCallBack(byte[] responseContent, String callBackdata);
	
	
	@Override
	public int hashCode() {
		String before = "";
		if(getParam() !=null){
			for(Entry<String,String> entryS : getParam().entrySet()){
				before += entryS.getKey();
				before += entryS.getValue();
			}
		} 
		if(getHeader() !=null){
			for(Entry<String,String> entryS : getHeader().entrySet()){
				before += entryS.getKey();
				before += entryS.getValue();
			}
		}
		String key = before + getUrl() + 
				getEtag() + getiMS();
		BigInteger hashValu = new BigInteger(String.valueOf(0));
		BigInteger pow27 = new BigInteger(String.valueOf(1)); ;
		for(int i = 0 ; i < key.length() ; i++){
			BigInteger letter = new BigInteger(String.valueOf(key.charAt(i) - 96)); 
			hashValu = hashValu.add(letter.multiply(pow27));
			pow27 = pow27.multiply(new BigInteger(String.valueOf(27)));
		}
		return hashValu.mod(new BigInteger(String.valueOf(200000))).intValue();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Request){
			Request<?> r2 = (Request<?>)o;
			return compareWith(r2);
		}
		return false;
	}
}
