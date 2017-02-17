package org.spontaneous.core.impl;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.GenericAsyncWebservice;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;

import java.util.ArrayList;
import java.util.List;

/**
 * Webservice for loading tracks from backend
 */
public class GetTracksWebService extends GenericAsyncWebservice {

    public void doRequest(WebServiceCallHandler requestResult) throws SystemException
    {
        WebServiceRequestConfig req = buildCreateTrackRequest();
        configureAndExecuteRequest(requestResult, req);
    }

    private WebServiceRequestConfig buildCreateTrackRequest() throws SystemException
    {
        final String enpointUrl = RestUrls.SERVER_NAME + ":" + RestUrls.PORT +
                RestUrls.REST_SERVICE_TRACKS.toString();

        WebServiceRequestConfig req = new WebServiceRequestConfig(WebServiceRequestConfig.Method.POST, enpointUrl);
        req.addToken(Authentication.INSTANCE.getToken());
        req.addAppInfo();

        return req;
    }

    @Override
    public void interpretResponse(WebServiceResponse response)
    {
        if (response.getStatus() == WebServiceResponse.Status.OK) {
            Log.i(TAG, "Getting tracks successful");
        }
    }

    /**
     * Generates list of tracks from web service response.
     *
     * @param response response that is interpreted
     * @return list of tracks
     */
    public static List<TrackModel> getTracksFromResponse(WebServiceResponse response)
    {
        List<TrackModel> tracks = new ArrayList<TrackModel>();
        if (response != null && response.haveJsonContent()) {
            try {
                JSONArray trackArrays = response.getJsonContent().getJSONArray("tracks");
                for (int i = 0; i < trackArrays.length(); i++) {
                    TrackModel trackModel = new TrackModel();
                    trackModel = trackModel.restoreFromJSON(trackArrays.getJSONObject(i));
                    tracks.add(trackModel);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error during reading response", e);
            }
        }
        return tracks;
    }
}
