package file;
import java.util.List;


public interface ConstPoolCalc {
	
	public long[] calcLength(Cache.Entry entry);
	
	public boolean isEmpty(Cache.Entry entry);
	
	public long[] parseIndexPool(byte[] indexPool);
	
	public List<byte[]> parseConstPool(long[] indexs,byte[] constPool);
	
	
	
}