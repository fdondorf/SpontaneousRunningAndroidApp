package org.spontaneous.core.impl;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.activities.model.UserModel;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.GenericSyncWebservice;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.ConfigProvider;
import org.spontaneous.core.crossdomain.UserInfo;

import java.util.concurrent.TimeoutException;

public class ChangeProfileWebService extends GenericSyncWebservice {

    private final UserModel userModel;

    /**
     * Initializes the web service object with the timeout value
     *
     * @param timeout the timeout value
     */
    public ChangeProfileWebService(int timeout, UserModel userModel)
    {
        super(timeout);
        this.userModel = userModel;
    }

    public WebServiceResponse doSynchronousRequest(WebServiceCallHandler requestResult) throws TimeoutException
    {
        WebServiceRequestConfig req = buildUpdateUserRequest(this.userModel);
        return configureAndExecuteSynchRequest(requestResult, req);
    }

    private WebServiceRequestConfig buildUpdateUserRequest(UserModel userModel)
    {
        String serverUrl = ConfigProvider.INSTANCE.getConfig(Authentication.INSTANCE.getConfigKey());
        final String enpointUrl = serverUrl +
                RestUrls.REST_SERVICE_UPDATE_USER.toString();

        WebServiceRequestConfig req = new WebServiceRequestConfig(WebServiceRequestConfig.Method.POST, enpointUrl);
        req.addToken(Authentication.INSTANCE.getToken());
        req.postAsJSON = true;
        req.setPostPayloadJson(createPostPayloadJson(userModel));
        req.addAppInfo();

        return req;
    }

    /**
     * Creates the JSON structure representing the user data
     *
     * @return The {@link JSONObject} representing the user data
     */
    public JSONObject createPostPayloadJson(UserModel userModel)
    {
        try {
            userModel.setId(Long.valueOf(String.valueOf(UserInfo.INSTANCE.getUserInfo().getUserId())));
            /*
            userModel.setFirstname(UserInfo.INSTANCE.getUserInfo().getFirstName());
            userModel.setLastname(UserInfo.INSTANCE.getUserInfo().getLastName());
            userModel.setEmail(UserInfo.INSTANCE.getUserInfo().getEmail());
            userModel.setGender(UserInfo.INSTANCE.getUserInfo().getGender());
            userModel.setProfileImage(UserInfo.INSTANCE.getUserInfo().getProfileImage());
            */
            return userModel.storeInJSON();
        } catch (JSONException e) {
            Log.e(TAG, "Cannot build JSON-Object 'parcels'", e);
        }
        return null;
    }

    @Override
    public void interpretResponse(WebServiceResponse response)
    {
        if (response.getStatus() == WebServiceResponse.Status.OK) {
            Log.i(TAG, "Changed profile successfully");
        }
    }

}
