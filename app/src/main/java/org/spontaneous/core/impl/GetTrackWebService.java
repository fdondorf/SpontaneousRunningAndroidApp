package org.spontaneous.core.impl;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.GenericAsyncWebservice;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;

/**
 * Webservice for loading track details from backend
 */
public class GetTrackWebService extends GenericAsyncWebservice {

    private Long trackId;

    public GetTrackWebService(Long trackId) {
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
                RestUrls.REST_SERVICE_TRACK_DETAILS.toString() + trackId;

        WebServiceRequestConfig req = new WebServiceRequestConfig(WebServiceRequestConfig.Method.POST, enpointUrl);
        req.addToken(Authentication.INSTANCE.getToken());
        req.addAppInfo();

        return req;
    }

    @Override
    public void interpretResponse(WebServiceResponse response)
    {
        if (response.getStatus() == WebServiceResponse.Status.OK) {
            Log.i(TAG, "Getting track details successful");
        }
    }

    /**
     * Generate track pojo from web service response.
     *
     * @param response response that is interpreted
     * @return  track pojo
     */
    public static TrackModel getTrackDetailsFromResponse(WebServiceResponse response)
    {
        TrackModel mTrack = new TrackModel();
        try {
            JSONObject to = response.getJsonContent().getJSONObject("trackDetails");
            mTrack = mTrack.restoreFromJSON(to);

        } catch (JSONException e) {
            Log.e(TAG, "Error during reading response", e);
        }
        return mTrack;
    }
}
