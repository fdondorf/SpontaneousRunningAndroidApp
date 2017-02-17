package org.spontaneous.core.common;

import android.util.Log;

import org.spontaneous.core.ws.clients.RESTEndpointTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Generic webservices with synchronous execution.
 *
 * Created by rstroh on 23.11.2015.
 */
public abstract class GenericSyncWebservice extends GenericWebService {

    /** Tag for debugging */
    public static final String TAG = GenericSyncWebservice.class.getSimpleName();

    /** Timeout in seconds. Requests will be cancelled after timeout. */
    private int webserviceTimeoutInSecs;

    /**
     * @param timeout Timeout in seconds
     */
    public GenericSyncWebservice(int timeout)
    {
        this.webserviceTimeoutInSecs = timeout;
    }

    /**
     * @param requestResult Webservice call handler
     * @param req           Request configuration object
     * @return Response of webservice
     * @throws TimeoutException Timeout of RestEndpoint-Task
     */
    protected WebServiceResponse configureAndExecuteSynchRequest(WebServiceCallHandler requestResult, WebServiceRequestConfig req)
            throws TimeoutException
    {
        requestResult.setService(this);
        requestResult.setTask(new RESTEndpointTask(req));

        try {
            return requestResult.getTask().execute().get(webserviceTimeoutInSecs, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Request interrupted", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "Cannot build proper request", e);
        }

        return null;
    }

    public void interpretResponse(WebServiceResponse response)
    {
        // Override when needed
    }

    /**
     * Starts synchronous webservice request.
     *
     * @param requestResult Webservice call handler
     * @return Response of webservice request
     * @throws SystemException Standard exception for webservices
     */
    protected abstract WebServiceResponse doSynchronousRequest(WebServiceCallHandler requestResult) throws TimeoutException;

}
