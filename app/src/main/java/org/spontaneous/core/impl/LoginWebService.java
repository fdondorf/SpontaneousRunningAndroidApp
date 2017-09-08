package org.spontaneous.core.impl;

import android.content.Context;
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
import org.spontaneous.utility.SecurityUtil;

import java.util.Date;

public class LoginWebService extends GenericAsyncWebservice {

    private Context ctx;

    public LoginWebService(Context ctx) {
        super();
        this.ctx = ctx;
    }

    public void doRequest(WebServiceCallHandler requestResult) throws SystemException
    {
        WebServiceRequestConfig req = buildLoginRequest(requestResult);
        configureAndExecuteRequest(requestResult, req);
    }

    private WebServiceRequestConfig buildLoginRequest(WebServiceCallHandler requestResult) throws SystemException
    {

        String serverUrl = ConfigProvider.INSTANCE.getConfig(Authentication.INSTANCE.getConfigKey());
        final String enpointUrl = serverUrl + RestUrls.REST_SERVICE_LOGIN.toString();

        WebServiceRequestConfig req = new WebServiceRequestConfig(WebServiceRequestConfig.Method.POST, enpointUrl);
        req.postAsJSON = false;

        setRequiredParams(req, new String[]{"username", "password"}, new String[]{"username", "password"});

        req.setParam("grant_type", "password");

        Context ctx = requestResult.getContext();

        if (ctx != null) {
            final String authBasicBase = SecurityUtil.getLoginAuthBasicBase(ctx);
            // To get know AUTH BASIC credentials generated from new random data
            // turn ON insecure logging and uncomment following lines
            // InsecureLog.i(TAG, "Auth basic USER: "+authBasicBase.substring(0, 32));
            // InsecureLog.i(TAG, "Auth basic PWD: "+authBasicBase.substring(32, 64));
            req.addAuthBasic(authBasicBase.substring(0, 32), authBasicBase.substring(32, 64));
        }

        return req;
    }

    @Override
    public void interpretResponse(WebServiceResponse response)
    {
        try {
            JSONObject json = response.getJsonContent();
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
        } catch (JSONException e) {
            Log.e(TAG, "Cannot deserialize Login WebService response JSON", e);
        }
    }

}
