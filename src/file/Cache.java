package file;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * 所有异常我不处理,所以要在缓存之前保证原子性
 * 
 * 
 * 这里唯一的问题就是要是数据太大那么可能内部抓不到异常,或者导致数据丢失
 * @author user
 *
 */
public abstract class Cache {
	
	public Cache(BasicCalculator basicCalculator, File cacheDir) {
		
	}
	
	public abstract void put(String requestKey,Cache.Entry entry) throws IOException ,IndexPoolOverflowException;
	
	public abstract Cache.Entry get(String requestKey) throws IOException ;
	
	public static class Entry{
		
		public String etag;	//外部进行处理,如果etag没有就设置为null
		
		public String iMS; //外部进行处理,如果last - modified没有就设置为null
		
		public long ttl;
		
		public long expires;
		
		public Map<String,String> headers;
		
		public byte[] datas;
		
	}
	
	public abstract boolean initialize();
	
	public abstract boolean delete(String requestKey);
	
	protected static Iterator<Object> iterator(Cache.Entry entry) {
		final List<Object> mlist = new ArrayList<Object>();
		mlist.add(entry.etag);
		mlist.add(entry.iMS);
		mlist.add(entry.ttl);
		mlist.add(entry.expires);
		mlist.add(entry.headers);
		mlist.add(entry.datas);
		
		return new Iterator<Object>(){
			
			private int record = -1;
			
			@Override
			public boolean hasNext() {
				record++;
				return record <= 5 ? true : false;
			}

			@Override
			public Object next() {
				return mlist.get(record);
			}

			@Override
			public void remove() {
				
			}
			
		};
	}
	
}
