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
import org.spontaneous.core.crossdomain.UserInfo;

public class SaveTrackWebService extends GenericAsyncWebservice {

    private final TrackModel trackModel;

    /**
     * Initializes the web service object with the track model
     *
     * @param trackModel the track to be saved
     */
    public SaveTrackWebService(TrackModel trackModel)
    {
        this.trackModel = trackModel;
    }

    public void doRequest(WebServiceCallHandler requestResult) throws SystemException
    {
        WebServiceRequestConfig req = buildCreateTrackRequest();
        configureAndExecuteRequest(requestResult, req);
    }

    private WebServiceRequestConfig buildCreateTrackRequest() throws SystemException
    {
        final String enpointUrl = RestUrls.SERVER_NAME + ":" + RestUrls.PORT +
                RestUrls.REST_SERVICE_TRACK_UPDATE_ALL.toString();

        WebServiceRequestConfig req = new WebServiceRequestConfig(WebServiceRequestConfig.Method.POST, enpointUrl);
        req.addToken(Authentication.INSTANCE.getToken());
        req.postAsJSON = true;
        req.setPostPayloadJson(createPostPayloadJson());
        req.addAppInfo();

        return req;
    }

    /**
     * Creates the JSON structure representing the track data
     *
     * @return The {@link JSONObject} representing the track data
     */
    public JSONObject createPostPayloadJson()
    {
        try {
            trackModel.setUserId(Integer.valueOf(String.valueOf(UserInfo.INSTANCE.getUserInfo().getUserId())));
            return trackModel.storeInJSON();
        } catch (JSONException e) {
            Log.e(TAG, "Cannot build JSON-Object 'parcels'", e);
        }
        return null;
    }

    @Override
    public void interpretResponse(WebServiceResponse response)
    {
        if (response.getStatus() == WebServiceResponse.Status.OK) {
            Log.i(TAG, "Saving track successful");
        }
    }

}
