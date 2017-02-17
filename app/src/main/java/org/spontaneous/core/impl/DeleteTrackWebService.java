package org.spontaneous.core.impl;

import android.util.Log;

import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.GenericAsyncWebservice;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;

public class DeleteTrackWebService extends GenericAsyncWebservice {

    private final Long trackId;

    /**
     * Initializes the web service object with the track id
     *
     * @param trackId the track to be saved
     */
    public DeleteTrackWebService(Long trackId)
    {
        this.trackId = trackId;
    }

    public void doRequest(WebServiceCallHandler requestResult) throws SystemException
    {
        WebServiceRequestConfig req = buildCreateTrackRequest();
        configureAndExecuteRequest(requestResult, req);
    }

    private WebServiceRequestConfig buildCreateTrackRequest() throws SystemException
    {
        final String enpointUrl = RestUrls.SERVER_NAME + ":" + RestUrls.PORT +
                RestUrls.REST_SERVICE_TRACK_DELETE.toString() + trackId;

        WebServiceRequestConfig req = new WebServiceRequestConfig(WebServiceRequestConfig.Method.POST, enpointUrl);
        req.addToken(Authentication.INSTANCE.getToken());
        req.postAsJSON = true;
        req.addAppInfo();

        return req;
    }

    @Override
    public void interpretResponse(WebServiceResponse response)
    {
        if (response.getStatus() == WebServiceResponse.Status.OK) {
            Log.i(TAG, "Deleting track successful");
        }
    }

}
