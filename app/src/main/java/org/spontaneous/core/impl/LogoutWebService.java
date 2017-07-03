package org.spontaneous.core.impl;

import android.util.Log;

import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.GenericAsyncWebservice;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.ConfigProvider;

public class LogoutWebService extends GenericAsyncWebservice {

    public void doRequest(WebServiceCallHandler requestResult) throws SystemException
    {
        WebServiceRequestConfig req = buildLogoutRequest();
        configureAndExecuteRequest(requestResult, req);
    }

    private WebServiceRequestConfig buildLogoutRequest()
    {
        String serverUrl = ConfigProvider.INSTANCE.getConfig(Authentication.INSTANCE.getConfigKey());
        final String enpointUrl = serverUrl +
                RestUrls.REST_SERVICE_LOGOUT; //ConfigProvider.INSTANCE.getConfig("logout_endpoint");

        WebServiceRequestConfig req = new WebServiceRequestConfig(WebServiceRequestConfig.Method.POST, enpointUrl);

        final String token = Authentication.INSTANCE.getToken();

        req.addToken(token);
        req.setQueryParam("token", token);
        return req;
    }

    @Override
    public void interpretResponse(WebServiceResponse response)
    {
        Log.i(TAG, response.getJsonContent().toString());
    }

}
