package file;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



public class BasicCalculator implements ConstPoolCalc {

	private static final int DEFULAT_LONGSTORAGE_LENGTH = 4 ;
	
	private static final int DEFULAT_INDEXPOOL_SIZE = 6;
	
	
	@Override
	public long[] calcLength(Cache.Entry entry) {
		long[] lengths = new long[DEFULAT_INDEXPOOL_SIZE];
		lengths[0] = strLength(entry.etag);
		lengths[1] = strLength(entry.iMS);
		lengths[2] = DEFULAT_LONGSTORAGE_LENGTH;
		lengths[3] = DEFULAT_LONGSTORAGE_LENGTH;
		lengths[4] = mapLength(entry.headers);
		lengths[5] = entry.datas.length;
		
		return lengths;
	}

	private long mapLength(Map<String, String> headers) {
		long length = 0;
		if(headers != null){
			for(Entry<String, String> entry : headers.entrySet()){
				length += 8;
				length += strLength(entry.getKey());
				length += strLength(entry.getValue());
			}
		} 
		return length;
	}
	

	private long strLength(String str){
		if(str == null){
			return 0;
		}
		try {
			return str.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
		}
		return 0;
	}
	

	@Override
	public boolean isEmpty(Cache.Entry entry) {
		Iterator<Object> iterator = Cache.iterator(entry);
		while(iterator.hasNext()){
			if(!isEmpty(iterator.next()))
				return false;
		}
		return true;
	}

	private boolean isEmpty(String str) {  
        return str == null || str.trim().length() == 0;  
    }  
	
	private boolean isEmpty(Map<?,?> map) {  
        return map.isEmpty();  
    }
	
	private boolean isEmpty(long l){
		return l == 0;
	}
	
	private boolean isEmpty(Object obj){
		return obj instanceof String ? isEmpty((String)obj) : 
			(obj instanceof Long) ? isEmpty((Long)obj) :  obj instanceof Map ? isEmpty((Map)obj) : false;  
		
	}

	@Override
	public long[] parseIndexPool(byte[] indexPool) {

		long[] indexs = new long[DEFULAT_INDEXPOOL_SIZE];
		for(int i = 0 ; i < indexs.length ; i++){
			byte[] temp = new byte[DEFULAT_LONGSTORAGE_LENGTH];
			for(int j = 0 ; j < temp.length ; j++){
				temp[j] = indexPool[i*5 + j + 1];
			}
			indexs[i] = BasicFileCache.readLong(temp);
		}
		return indexs;
	}

	@Override
	public List<byte[]> parseConstPool(long[] indexs, byte[] constPool) {
		/*for(long s : indexs)
			System.out.println(s);*/
		List<byte[]> mlist = new ArrayList<byte[]>();
		int forword = 0;
		for(int i = 0 ; i < indexs.length ; i++ ){
			byte[] temp = new byte[((int)indexs[i])];
			for(int j = forword ; j < forword + indexs[i] ;j++){
				temp[j - forword] = constPool[j];
			}
			forword += indexs[i];
			mlist.add(temp);
		}
		
		return mlist;
	}

}
