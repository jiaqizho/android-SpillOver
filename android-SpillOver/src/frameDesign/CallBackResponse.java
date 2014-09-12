package frameDesign;

import java.util.concurrent.Executor;

public class CallBackResponse implements ResponseHandler{
	
	private Executor mResponsePoster;
	
	/**
	 * new android.os.Handler(Looper.getMainLooper());
	 * @param handler
	 */
    public CallBackResponse(final android.os.Handler handler) {
        // Make an Executor that just wraps the handler.
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
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
				request.listener.callErrorBack();
			}
		});
	}


	@Override
	public void callBack(final Request<?> request,final byte[] responseContent,
			final String callBackdata) {
		mResponsePoster.execute(new Runnable() {
			
			@Override
			public void run() {
				request.listener.callBack(request.handlerCallBack(responseContent,callBackdata));
			}
		});		
	}
    
    
}
