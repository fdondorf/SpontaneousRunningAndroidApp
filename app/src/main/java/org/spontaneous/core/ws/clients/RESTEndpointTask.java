package org.spontaneous.core.ws.clients;

import android.os.AsyncTask;
import android.util.Log;

import org.spontaneous.core.common.error.ErrorType;
import org.spontaneous.core.common.error.SystemError;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.common.WebServiceResultsHandler;
import org.spontaneous.core.common.WebServiceProgressHandler;

/**
 * Task to call a JSON Endpoint asynchronously.
 * Supports cancellation.
 */
public class RESTEndpointTask extends AsyncTask<Void, Void, WebServiceResponse> {

    public static final String TAG = RESTEndpointTask.class.getSimpleName();

    private WebServiceRequestConfig request;

    WebServiceResultsHandler resultsHandler = null;
    WebServiceProgressHandler progressHandler = null;
    private RESTEndpointClient webClient;

    /**
     * Proper cancellation handling needs to check if the user hasn't cancelled
     * the Task before constructing the HTTPClient.
     */
    private boolean mCancelRequested = false;
    private static final Object LOCK = new Object[0];

    public RESTEndpointTask(WebServiceRequestConfig req)
    {
        this.request = req;
    }

    public void setResultsHandler(WebServiceResultsHandler callback)
    {
        resultsHandler = callback;
    }

    public void setProgressHandler(WebServiceProgressHandler callback)
    {
        progressHandler = callback;
    }

    public void abortTask()
    {
        synchronized (LOCK) {
            if (webClient != null) {
                webClient.cancel();
            }
            mCancelRequested = true;
        }
    }

    @Override
    protected void onCancelled()
    {
        Log.v(TAG, "onCancelled() called.");
        abortTask();
        Log.v(TAG, "onCancelled() cancelled underlying HTTPClient.");
        super.onCancelled();
    }

    @Override
    protected void onCancelled(WebServiceResponse result)
    {
        Log.v(TAG, "onCancelled() called.");
        abortTask();
        Log.v(TAG, "onCancelled() cancelled underlying HTTPClient.");
        super.onCancelled(result);
    }


    @Override
    protected WebServiceResponse doInBackground(Void... params)
    {
        // Delegate the response back to the TaskFragment.
        // All errors are delegated as well.
        //
        // It's necessary to get back on the UI Thread.

        // Bugfix for a race condition:
        // User can cancel the process just before mClient
        // is constructed. This would normally lead to
        // NullPointerException - without the synchronization
        // below.
        // It did happen in tests on emulator.
        synchronized (LOCK) {
            if (!mCancelRequested) {
                webClient = new RESTEndpointClient();
            } else {
                Log.d(TAG, "Task cancelled before the request was invoked.");
                return new WebServiceResponse.Builder().fail(SystemError.USER_CANCELED).build();
            }
        }


        // Invoke the request.
        // Cancellation here is implemented according to HTTPClient
        // recommendation: calling abort() on the underlying request.

        return webClient.request(request);

    }

    @Override
    protected void onPreExecute()
    {
        if (progressHandler != null) {
            progressHandler.showWebServiceProgress();
        }
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(WebServiceResponse response)
    {
        // User could've pressed Home button or launched other application.
        if (resultsHandler == null) {
            return;
        }

        if (progressHandler != null) {
            progressHandler.hideWebServiceProgress();
        }

        // User could have canceled the task
        if (mCancelRequested) {
            Log.d(TAG, "JSONEndpointPostTask was cancelled. Callback calling suppressed.");

            resultsHandler.onError(new WebServiceResponse.Builder().fail(SystemError.USER_CANCELED).build());

            return;
        } else {

            handleResponse(response);

        }

    }

    public void handleResponse(WebServiceResponse response)
    {
        resultsHandler.onAnyResult(response);

        if (response == null) {

            resultsHandler.onError(new WebServiceResponse.Builder().fail(new SystemError(ErrorType.EMPTY_RESPONSE)).build());

        } else {

            switch (response.getStatus()) {
                case ERROR:
                    handleResponseError(response);
                    break;
                case OK:
                    resultsHandler.onResponseSuccessful(response);
                    break;
                case UNDEFINED:
                default:
                    resultsHandler.onError(new WebServiceResponse.Builder().fail(new SystemError(ErrorType.UNDEFINED_STATUS)).build());
                    break;
            }

        }
    }

    public void handleResponseError(WebServiceResponse response)
    {
        switch (response.getError().getType().getCategory()) {
            case AUTH:
                resultsHandler.onResponseUnauthorized(response);
                break;
            case CLIENT:
                resultsHandler.onBusinessError(response);
                break;
            case BACKEND:
            default:
                resultsHandler.onError(response);
                break;
        }
    }


    public WebServiceRequestConfig getRequest()
    {
        return request;
    }
}