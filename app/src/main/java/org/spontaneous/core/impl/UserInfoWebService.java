package org.spontaneous.core.impl;

import android.util.Log;

import org.json.JSONException;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.GenericAsyncWebservice;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.ConfigProvider;
import org.spontaneous.core.crossdomain.UserInfo;
import org.spontaneous.core.dao.UserDAO;

public class UserInfoWebService extends GenericAsyncWebservice {
    public void doRequest(WebServiceCallHandler requestResult) throws SystemException
    {
        WebServiceRequestConfig req = buildUserInfoRequest();
        configureAndExecuteRequest(requestResult, req);
    }

    private WebServiceRequestConfig buildUserInfoRequest() throws SystemException
    {
        String serverUrl = ConfigProvider.INSTANCE.getConfig(Authentication.INSTANCE.getConfigKey());
        final String enpointUrl = serverUrl +
                RestUrls.REST_SERVICE_USERINFO.toString();

        WebServiceRequestConfig req = new WebServiceRequestConfig(WebServiceRequestConfig.Method.POST, enpointUrl);
        req.addToken(Authentication.INSTANCE.getToken());
        req.addAppInfo();

        return req;
    }

    @Override
    public void interpretResponse(WebServiceResponse response)
    {
        try {

            UserDAO userInfo = new UserDAO().restoreFromJSON(response.getJsonContent());
            UserInfo.INSTANCE.setUserInfo(userInfo);

        } catch (JSONException e) {
            Log.e(TAG, "Cannot deserialize UserInfo WebService response JSON", e);
        }
    }

}
