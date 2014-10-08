package frameDesign;

import java.util.Map;

import android.util.Log;


public class RequestComparator implements Comparator{

	public static int diaoyongcishu = 0;
	
	@Override
	public boolean compare(Comparabler comp1, Comparabler comp2) {
		if((comp1 instanceof Request) && (comp2 instanceof Request)){
			Request<?> r1 = (Request<?>)comp1;
			Request<?> r2 = (Request<?>)comp2;
			if(r1.getUrl() == r2.getUrl() && compareMapByKeySet(r1.getParam(),r2.getParam()) 
					&& compareMapByKeySet(r1.getHeader(),r2.getHeader())){
				return true;
			}
		} 
		return false; 
	}
	
	 public static boolean compareMapByKeySet(Map<String,String> map1,Map<String,String> map2){  
		 if(map1 == null && map2 != null){
			 return false;
		 } else if(map1 != null && map2 == null){
			 return false;
		 } else if(map1 == null && map2 == null){
			 return true;
		 }
		 if(map1.size()!=map2.size()){    
			 return false;  
		 }  
		 String tmp1;  
		 String tmp2;  
		 boolean b=false;  
		 for(String key:map1.keySet()){  
			 if(map2.containsKey(key)){  
				 tmp1=map1.get(key);  
				 tmp2=map2.get(key);  
				 if(null!=tmp1 && null!=tmp1){   
	                      
					 if(tmp1.equals(tmp2)){  
						 b=true;  
						 continue;  
					 }else{  
						 b=false;  
						 break;  
					 }  
	                      
				 }else if(null==tmp1 && null==tmp2){    
					 b=true;  
					 continue;  
				 }else{  
					 b=false;  
					 break;  
				 }  
			 }else{  
				 b=false;  
				 break;  
			 }  
		 }  
		 return b;  
	 }  
}
