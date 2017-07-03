package org.spontaneous.core.impl;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.GenericSyncWebservice;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.ConfigProvider;
import org.spontaneous.core.crossdomain.UserInfo;

import java.util.concurrent.TimeoutException;

public class SaveTrackWebService extends GenericSyncWebservice {

    private final TrackModel trackModel;

    /**
     * Initializes the web service object with the timeout value
     *
     * @param timeout the timeout value
     */
    public SaveTrackWebService(int timeout, TrackModel trackModel)
    {
        super(timeout);
        this.trackModel = trackModel;
    }

    public WebServiceResponse doSynchronousRequest(WebServiceCallHandler requestResult) throws TimeoutException
    {
        WebServiceRequestConfig req = buildCreateTrackRequest();
        return configureAndExecuteSynchRequest(requestResult, req);
    }

    private WebServiceRequestConfig buildCreateTrackRequest()
    {
        String serverUrl = ConfigProvider.INSTANCE.getConfig(Authentication.INSTANCE.getConfigKey());
        final String enpointUrl = serverUrl +
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
