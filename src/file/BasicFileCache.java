package file;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;



public class BasicFileCache extends Cache{
	
	private File cacheDir ; 
	
	private ConstPoolCalc mConstPoolCalc;	//用来计算索引池字节数
	
	protected static final long DEFULAT_VERSION_CODE = 1407573710;	//2014/8/9 16:41:50

	protected static final long DEFULAT_INDEXPOOL_SIZE = 6;	//索引池变量个数
	
	private static final int DEFULAT_LONGSTORAGE_LENGTH = 4 ;
	
	private static final int DEFULAT_INDEXPOLL_LENGTH = 30;	//索引池子大小
	
	protected int mIndexNumbers = 0 ;
	
	public BasicFileCache(File cacheDir) {
		this(new BasicCalculator(),cacheDir);
	}

	public BasicFileCache(BasicCalculator basicCalculator, File cacheDir) {
		super(basicCalculator,cacheDir);
		this.mConstPoolCalc = basicCalculator;
		this.cacheDir = cacheDir;
	}

	/**
	 * 非线程安全
	 */
	@Override
	public void put(String requestKey, Cache.Entry entry) throws IOException, IndexPoolOverflowException {
		try {
			File file = getFileForKey(requestKey);
			FileOutputStream out = new FileOutputStream(file);
			
			writerHeader(out);
			if(mConstPoolCalc.isEmpty(entry)){
				throw new IOException("Entry is empty");
			} 
			
			if(mConstPoolCalc.calcLength(entry).length != DEFULAT_INDEXPOOL_SIZE){
				throw new IndexPoolOverflowException();
			}
			
			long[] lengths = mConstPoolCalc.calcLength(entry);
			for(long length : lengths){
				writeIndexPool(out,length);
			}
			writeConstPool(lengths,out,entry);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			mIndexNumbers = 0;
		}
		
	}
	
	public File getFileForKey(String key) {
		if(!cacheDir.exists()){
			cacheDir.mkdirs();
		}
		return new File(cacheDir, getFilenameForKey(key));
	}
	

    private String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }

	private void writeConstPool(long[] lengths, OutputStream out,
		Entry entry) throws IOException {
		
		writeString(lengths[0],out,entry.etag);
		writeString(lengths[1],out,entry.iMS);
		writelong(out,entry.ttl);
		writelong(out, entry.expires);
		writeHeads(lengths[4],out,entry.headers);
		out.write(entry.datas, 0, entry.datas.length);
	}


	@Override
	public Cache.Entry get(String requestKey) throws IOException {
		byte[] version = new byte[DEFULAT_LONGSTORAGE_LENGTH];
		byte[] indexPool = new byte[DEFULAT_INDEXPOLL_LENGTH];
		byte[] constPool = null;
		File file = getFileForKey(requestKey);
		if(!file.exists()){
			return null;
		}
		FileInputStream in = new FileInputStream(file);
		
		byte[] datas =  streamToBytes(in,(int)file.length());
		constPool = dispatchData(datas,version,indexPool);
		if(DEFULAT_VERSION_CODE > readLong(version)){
			throw new IOException("version is wrong");
		}
		long[] indexs = mConstPoolCalc.parseIndexPool(indexPool);
		List<byte[]> mList = mConstPoolCalc.parseConstPool(indexs, constPool);
		Cache.Entry entry = new Cache.Entry();
		setEnrty(entry,mList);
		
		return entry;
	}
	
	
	public boolean delete(String requestKey){
		return getFileForKey(requestKey).delete();
	}
	
	private void setEnrty(Entry entry, List<byte[]> mList) {
		entry.etag = new String(mList.get(0));
		entry.iMS = new String(mList.get(1));
		entry.ttl = readLong(mList.get(2));
		entry.expires = readLong(mList.get(3));
		entry.headers = parseHeaders(mList.get(4));
		entry.datas = mList.get(5);
	}

	private Map<String, String> parseHeaders(byte[] headers) {
		Map<String, String> map = new HashMap<String, String>();
		List<String> mList = new ArrayList<String>();
		int colum = 0;
		while(true){
			try{
				byte[] temp = new byte[DEFULAT_LONGSTORAGE_LENGTH];
				for(int i = 0 ; i < temp.length ; i++,colum++){
					temp[i] = headers[colum];
				}
					
				int length = (int)readLong(temp);
				if(length == 0){
					mList.add(null);
					continue;
				} 
				
				byte[] data = new byte[length];
				for(int k = 0 ;k < length ; k++,colum++){
					data[k] = headers[colum];
				}
				mList.add(new String(data));
			} catch (ArrayIndexOutOfBoundsException e ){
				break;
			}
		}
		
		int key = 0 ;
		for(int i = 0 ; i < (mList.size()/2) ; i++){
			map.put(mList.get(key), mList.get(key + 1));
			key += 2;
		}
		
		return map;
	}

	protected byte[] dispatchData(byte[] datas, byte[] version, byte[] indexPool) {
		if(version == null){
			version = new byte[DEFULAT_LONGSTORAGE_LENGTH];			
		}  
		
		if(indexPool == null){
			indexPool = new byte[DEFULAT_INDEXPOLL_LENGTH];
		}
		
		for(int i = 0 ; i < DEFULAT_LONGSTORAGE_LENGTH ;i++){
			version[i] = datas[i];
		}
		
		for(int i = DEFULAT_LONGSTORAGE_LENGTH ; i < DEFULAT_INDEXPOLL_LENGTH + DEFULAT_LONGSTORAGE_LENGTH ; i++){
			indexPool[i - DEFULAT_LONGSTORAGE_LENGTH] = datas[i];
		}
		
		byte[] constPool = new byte[datas.length - DEFULAT_INDEXPOLL_LENGTH - DEFULAT_LONGSTORAGE_LENGTH];
		for(int i = (DEFULAT_LONGSTORAGE_LENGTH + DEFULAT_INDEXPOLL_LENGTH) ; i < datas.length ; i++){
			constPool[i - (DEFULAT_LONGSTORAGE_LENGTH + DEFULAT_INDEXPOLL_LENGTH)] = datas[i]; 
		}
		
		return constPool; 
	}


	static long readLong(byte[] bytes){
		
		long n = 0;
		n |= (bytes[0] << 24);
		n |= ((bytes[1] << 16) & 0x00FFFFFF);
		n |= ((bytes[2] << 8) & 0x0000FFFF);
		n |= ((bytes[3] << 0) & 0x000000FF);
		return n;
	}
	
	
	protected void writeString(long length,OutputStream out,String str) throws IOException {
		if(length == 0 && str == null){
			return ;
		}
		byte[] b = str.getBytes("UTF-8");
		if(length != b.length)
			throw new IOException("IndexPool is out of step with ConstanPool");
		out.write(b, 0, b.length);
	}
	
	private void writeHeads(long length , OutputStream out , Map<String,String> map) throws IOException {
		if(length == 0 && map ==null){
			writelong(out, 0);
		}else {
			for(java.util.Map.Entry<String, String> headers :map.entrySet()){
				writeStringInConst(out,headers.getKey());
				writeStringInConst(out,headers.getValue());
			}
		}
	}
	
	private void writeStringInConst(OutputStream out, String key) throws IOException {
		if(key == null){
			return;
		}
		byte[] bytes = key.getBytes("UTF-8");
		writelong(out, bytes.length);
		out.write(bytes, 0, bytes.length);
	}


	protected void writeIndexPool(OutputStream out , long length) throws IndexPoolOverflowException {
		if(mIndexNumbers < DEFULAT_INDEXPOOL_SIZE){
			try {
				writeIndexHead(out);
				writelong(out, length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new IndexPoolOverflowException();
		}
	}

	private void writeIndexHead(OutputStream out) throws IOException{
		out.write((byte)mIndexNumbers);
		mIndexNumbers++;
	}

	private void writelong(OutputStream out , long l) throws IOException {
		out.write((byte)(l >>> 24));
		out.write((byte)(l >>> 16));
		out.write((byte)(l >>> 8));
		out.write((byte)(l >>> 0));
	}
	
    
    protected void writerHeader(OutputStream out, long version) {
    	try {
			writelong(out , version);
		} catch (IOException e) {
		}
	}
    
    
    protected void writerHeader(OutputStream out) {
		writerHeader(out,DEFULAT_VERSION_CODE);
	}


    static byte[] streamToBytes(InputStream in, int length) throws IOException {
        byte[] bytes = new byte[length];
        int count;
        int pos = 0;
        while (pos < length && ((count = in.read(bytes, pos, length - pos)) != -1)) {
            pos += count;
        }
        if (pos != length) {
            throw new IOException("Expected " + length + " bytes, read " + pos + " bytes");
        }
        return bytes;
    }

	@Override
	public boolean initialize() {
		// TODO Auto-generated method stub
		//进行一些数据初始化操作:比如设置一个数据文件过期时间,数据文件是否损坏等排查,如果这里抛出异常,那么进行停职cache队列,或者全部不进行cache的操作;
		return true;
	}

}