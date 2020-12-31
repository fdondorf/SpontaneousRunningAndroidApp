package org.spontaneous.core.common;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class WebServiceRequestConfig {
    public static final String TAG = WebServiceRequestConfig.class.getSimpleName();

    public enum Method {
        GET,
        POST
    }

    Method method = Method.GET;
    String endpointURL;
    String serverThumbprints;
    public boolean postAsJSON = true;

    JSONObject postPayloadJson = new JSONObject();
    Bundle queryParams = new Bundle();
    Bundle postUrlParams = new Bundle();
    List<Header> headers = new LinkedList<>();
    private boolean spoiled = false;

    public WebServiceRequestConfig(Method method, String endpointURL)
    {
        super();
        this.method = method;
        this.endpointURL = endpointURL;
        //this.serverThumbprints = ConfigProvider.INSTANCE.getConfig("server_thumbprints");
    }

    public Method getMethod()
    {
        return method;
    }

    public void setMethod(Method method)
    {
        this.method = method;
    }

    public String getEndpointURL()
    {
        return endpointURL;
    }

    public void setEndpointURL(String endpointURL)
    {
        this.endpointURL = endpointURL;
    }

    public JSONObject getPostPayloadJson()
    {
        return postPayloadJson;
    }

    public void setPostPayloadJson(JSONObject postPayloadJson)
    {
        this.postPayloadJson = postPayloadJson;
    }

    public void setParam(String paramName, String value)
    {
        if (method == Method.POST) {
            if (this.postAsJSON) {
                setPayloadParam(paramName, value);
            } else {
                this.postUrlParams.putString(paramName, value);
            }
        } else {
            setQueryParam(paramName, value);
        }
    }

    public void setQueryParam(String paramName, String value)
    {
        this.queryParams.putString(paramName, value);
    }

    public void setPayloadParam(String paramName, String value)
    {
        try {
            this.postPayloadJson.put(paramName, value);
        } catch (JSONException e) {
            Log.d(TAG, "Something went wrong while adding POST request param " + paramName, e);
        }
    }

    public void setPayloadParam(String paramName, JSONObject value)
    {
        try {
            this.postPayloadJson.put(paramName, value);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong while adding POST request param " + paramName, e);
        }
    }

    public List<Header> getHeaders()
    {
        return headers;
    }

    public String buildRequestUrl()
    {
        Uri.Builder builder = Uri.parse(this.endpointURL).buildUpon();
        for (String key : queryParams.keySet()) {
            builder.appendQueryParameter(key, queryParams.getString(key));
        }
        return builder.build().toString();
    }

    public String getPostPayloadUrl()
    {
        Uri.Builder builder = Uri.parse(this.endpointURL).buildUpon();
        for (String key : postUrlParams.keySet()) {
            builder.appendQueryParameter(key, postUrlParams.getString(key));
        }
        return builder.build().getQuery();
    }


    public void addHeader(String headerName, String headerValue)
    {
        this.headers.add(new BasicHeader(headerName, headerValue));
    }

    public void addAuthBasic(String authBasicUser, String authBasicPass)
    {
        try {
            final String basicPayload = authBasicUser + ":" + authBasicPass;
            final String basicEncoded = new String(Base64.encode(basicPayload.getBytes(), Base64.NO_WRAP), "UTF8");
            //this.addHeader("Authorization", "Basic " + basicEncoded);
            this.addHeader(authBasicUser, authBasicPass);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "SOMETHING WENT TERRIBLY WRONG. THIS SHOULD NEVER HAPPEN", e);
        }

    }

    public void addToken(String token)
    {
        this.addHeader("Authorization", "Bearer " + token);
    }

    public void addAppInfo()
    {
        //this.setParam("apiVersion", ConfigProvider.INSTANCE.getConfig("api_version"));
        //this.setParam("appVersion", ConfigProvider.INSTANCE.getConfig("app_version"));
        //this.setParam("appKey", SecurityUtil.getAppKey(this.get);//ConfigProvider.INSTANCE.getAppKey());
        this.setParam("appSystem", "android");
    }

    public void spoilPayload()
    {
        spoiled = true;
    }

    public boolean isSpoiled()
    {
        return spoiled;
    }

    public String getServerThumbprints()
    {
        return serverThumbprints;
    }

    public void setServerThumbprint(String serverThumbprints) {
        this.serverThumbprints = serverThumbprints;
    }

}
