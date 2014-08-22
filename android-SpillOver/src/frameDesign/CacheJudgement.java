package frameDesign;


public class CacheJudgement {
	public boolean hasTTl(long ttl){
		return (System.currentTimeMillis() /1000) < ttl; 
	}

	public boolean hasExpired(long expires) {
		return (System.currentTimeMillis() /1000) < expires;
	}

	public boolean usefulEtag(String etag) {
		return etag != null;
	}

	public boolean usefulIMS(String iMS) {
		return iMS != null;
	}
}
