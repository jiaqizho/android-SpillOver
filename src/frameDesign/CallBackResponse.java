package frameDesign;

import java.io.IOException;
import java.util.concurrent.Executor;

import android.util.Log;

import file.Cache;

public class CallBackResponse implements ResponseHandler{
	
	private Executor mResponsePoster;
    
	private ResponseParse parse;
	
	private Cache mCache;
	
	private Object lock = new Object();
	
    public CallBackResponse(final android.os.Handler handler,ResponseParse parse, Cache cache) {
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
        this.parse = parse;
        this.mCache = cache;
    }
    
    
    @Deprecated
    public CallBackResponse(Executor executor) {
        mResponsePoster = executor;
    }

	@Override
	public void callErrorBack(final Request<?> request) {
		mResponsePoster.execute(new Runnable() {
			
			@Override
			public void run() {
				Cache.Entry entry = null;
				try {
					Log.i("DemoLog", request.getUrl());
					synchronized(lock){
						entry = mCache.get(request.getUrl());
					}
					
					if(entry == null){
						try{
							request.listener.callErrorBack(null,null);
						} catch (NullPointerException e){
						}
						return;
					}
					
					String data = parse.byteToEntity(entry.datas, entry.headers);
					try{
						request.listener.callErrorBack(entry.datas,data);
					} catch (NullPointerException e){
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}


	@Override
	public void callBack(final Request<?> request,final Response response) {
		mResponsePoster.execute(new Runnable() {
			
			@Override
			public void run() {
				try{
					request.listener.callBack(request.handlerCallBack(response.getDatas(),response.getCallBackdata()));
				} catch (NullPointerException e){
				}
			}
		});				
	}
    
    
}
