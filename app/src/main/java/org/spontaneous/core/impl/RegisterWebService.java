package org.spontaneous.core.impl;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.GenericAsyncWebservice;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.ConfigProvider;

/**
 * Created by fdondorf on 22.11.2016.
 */

public class RegisterWebService extends GenericAsyncWebservice {

    public void doRequest(WebServiceCallHandler requestResult) throws SystemException
    {
        WebServiceRequestConfig req = buildRegisterRequest();
        configureAndExecuteRequest(requestResult, req);
    }

    private WebServiceRequestConfig buildRegisterRequest() throws SystemException
    {
        String serverUrl = ConfigProvider.INSTANCE.getConfig(Authentication.INSTANCE.getConfigKey());
        final String enpointUrl = serverUrl +
                RestUrls.REST_SERVICE_REGISTER.toString();

        WebServiceRequestConfig req = new WebServiceRequestConfig(WebServiceRequestConfig.Method.POST, enpointUrl);
        req.postAsJSON = true;
        setRequiredParams(req, new String[]{"firstname", "lastname", "email", "password"},
                new String []{"firstname", "lastname", "email", "password"});
        req.addAppInfo();

        return req;
    }

    @Override
    public void interpretResponse(WebServiceResponse response)
    {
        try {
            JSONObject json = response.getJsonContent();
            Log.i(TAG, "Got response: " + json.toString());
            String firstname = json.getString("firstname");

            /*
            if (json != null) {
                if (json.has("access_token") && json.has("expires_in")) {
                    Authentication.INSTANCE.updateToken(
                            json.getString("access_token"),
                            new Date(new Date().getTime() +
                                    json.getInt("expires_in") * 1000));
                } else {
                    Log.e(TAG, "Missing Login WebService response JSON fields: access_token or expires_in");
                }
            } else {
                Log.e(TAG, "Invalid Login WebService response JSON");
            }
            */
        } catch (JSONException e) {
            Log.e(TAG, "Cannot deserialize Login WebService response JSON", e);
        }
    }
}
