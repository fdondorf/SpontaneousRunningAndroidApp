
package org.spontaneous.core.ws.clients;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.common.WebServiceResponse.ContentStatus;
import org.spontaneous.core.common.error.ErrorType;
import org.spontaneous.core.common.error.SystemError;
import org.spontaneous.utility.NetworkUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.auth.UsernamePasswordCredentials;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.AbstractHttpClient;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class RESTEndpointClient {

    public static final String TAG = RESTEndpointClient.class.getSimpleName();
    private static final Object LOCK = new Object();

    private static final class HTTPResponseCodes {
        // public static final int INFORMATIONAL = 100;
        public static final int SUCCESS = 200;
        // public static final int REDIRECTION = 300;
        public static final int CLIENT_ERROR = 400;
        // public static final int AUTHORIZATION_ERROR = 401;
        // public static final int SERVER_ERROR = 500;
    }

    private HttpUriRequest rawRequest;
    private volatile boolean cancelled;

    public WebServiceResponse request(WebServiceRequestConfig requestConfig)
    {
        AbstractHttpClient httpClient;

        synchronized (LOCK) {

            httpClient = buildClient(requestConfig);
            rawRequest = buildRequest(requestConfig);
            decorateRequest(requestConfig);

            if (cancelled) {
                rawRequest.abort();
            }
        }


        final WebServiceResponse.Builder resBuilder = new WebServiceResponse.Builder();
        handleResponse(requestConfig, httpClient, resBuilder);
        return resBuilder.build();
    }

    private AbstractHttpClient buildClient(WebServiceRequestConfig requestConfig)
    {
        DefaultHttpClient httpClient;
        if (!requestConfig.getEndpointURL().startsWith("https://")) {
            Log.d(TAG, "Constructing basic HTTP client");
            httpClient = new DefaultHttpClient();
            //httpClient = HttpClientBuilder.create()..build();
            httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials("spontaneous-client", "spontaneous-secret"));

        } else {
            Log.d(TAG, "Constructing secure HTTPS client");
            httpClient = HttpBasicAuthClient.createSSLClient(requestConfig);
        }
        return httpClient;
    }

    private HttpUriRequest buildRequest(WebServiceRequestConfig requestConfig)
    {
        switch (requestConfig.getMethod()) {
            case POST:
                return buildPostRequest(requestConfig);
            case GET:
            default:
                return buildGetRequest(requestConfig);
        }
    }

    private void decorateRequest(WebServiceRequestConfig requestConfig)
    {
        if (requestConfig.getHeaders().size() > 0) {
            rawRequest.setHeaders(requestConfig.getHeaders().toArray(new Header[0]));
        }
    }

    private HttpPost buildPostRequest(WebServiceRequestConfig requestConfig)
    {
        if (requestConfig.postAsJSON) {
            return buildJSONPostRequest(requestConfig);
        } else {
            return buildClassicPostRequest(requestConfig);
        }
    }

    private HttpPost buildJSONPostRequest(WebServiceRequestConfig requestConfig)
    {
        HttpPost request = new HttpPost(requestConfig.buildRequestUrl());

        Log.i("REQ URL", requestConfig.buildRequestUrl());
        StringEntity entity = convertToEntity(requestConfig.getPostPayloadJson());
        if (requestConfig.isSpoiled()) {
            Log.v("REQ SPOIL", "we spiled that request");
            try {
                entity = new StringEntity("{mallformed for debug purposes}");
            } catch (UnsupportedEncodingException e) {
                Log.v("REQ SPOIL", "Encoding not supported", e);
            }
        }
        Log.v("REQ", requestConfig.getPostPayloadJson().toString());

        entity.setContentType("application/json;charset=UTF-8");
        request.setEntity(entity);

        return request;
    }

    private HttpPost buildClassicPostRequest(WebServiceRequestConfig requestConfig)
    {
        HttpPost request = new HttpPost(requestConfig.buildRequestUrl());
        Log.i("REQ URL", requestConfig.buildRequestUrl());
        StringEntity entity;
        try {
            entity = new StringEntity(requestConfig.getPostPayloadUrl());
            entity.setContentType("application/x-www-form-urlencoded");
            request.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            Log.e("REQ", "Cannot build request", e);
        }
        Log.v("REQ", requestConfig.getPostPayloadUrl());
        return request;
    }

    private HttpGet buildGetRequest(WebServiceRequestConfig requestConfig)
    {
        Log.i("REQ URL", requestConfig.buildRequestUrl());
        return new HttpGet(requestConfig.buildRequestUrl());
    }

    /**
     * Main handler, prepares request and executes it, routing response
     * to response handler.
     */
    private void handleResponse(WebServiceRequestConfig requestConfig,
                                AbstractHttpClient httpClient, final WebServiceResponse.Builder resBuilder)
    {
        try {
            Log.d(TAG, "Sending request to " + requestConfig.getEndpointURL());
            HttpResponse response = httpClient.execute(rawRequest);
            Log.d(TAG, "Response arrived ");

            final int responseCode = parseStatusCode(response);
            final int normalizedResponseCode = (int) (responseCode / 100) * 100;
            final String responseContent = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";

            // TODO
            //if (Log.i) {
                Log.v("RESPONSE STATUS", response.getStatusLine().toString());
                Log.v("RESPONSE PAYLOAD", responseContent);
                Log.v("RESPONSE HEADERS", NetworkUtil.headersToString(response.getAllHeaders()));
            //}

            if (normalizedResponseCode == HTTPResponseCodes.SUCCESS) {
                handleCorrectResponse(resBuilder, response, responseContent);
            } else if (responseCode >= HTTPResponseCodes.CLIENT_ERROR) {
                handleErrorResponse(resBuilder, response, responseContent, responseCode);
            }
        } catch (ClientProtocolException e) {
            resBuilder.fail(new SystemError(ErrorType.CLIENT_PROTOCOL_ERROR, e.getMessage()));
        } catch (IOException e) {
            // Very important: Canceling the HTTPClient (the Android one) does
            // set the interrupted flag on thread. It does interfere with the
            // ExecutorService. The flag must be cleared, otherwise the thread
            // can't be reused. That's in accord with the Executor contract.

            // The call restores the "interrupted" flag.
            Thread.interrupted();

            Log.d(TAG, "IOException during service call", e);

            // Now, let the client code handle the error condition.
            resBuilder.fail(new SystemError(ErrorType.NETWORK_ERROR, e.getMessage()));
        }
    }

    /**
     * Standard handling of successful (HTTP 200) responses.
     */
    private void handleCorrectResponse(WebServiceResponse.Builder resBuilder, HttpResponse response, String responseContent)
            throws IOException
    {
        Header contentType = response.getFirstHeader("Content-Type");
        resBuilder.success();

        if (responseContent != null) {

            if ((contentType != null) && (contentType.getValue().contains("json"))) {
                try {

                    resBuilder.setJSONContent(new JSONObject(responseContent));

                } catch (JSONException e) {
                    String substring;
                    if (responseContent.length() > 60) {
                        substring = responseContent.substring(0, 60);
                    } else {
                        substring = responseContent;
                    }

                    resBuilder.fail(new SystemError(ErrorType.INVALID_RESPONSE_JSON,
                            "Error while parsing responseContent. Expected valid JSON String, got: \""
                                    + substring + "\""));
                }

            } else {
                resBuilder.setContentStatus(ContentStatus.UNKNOWN);
            }

        } else {
            resBuilder.setContentStatus(ContentStatus.EMPTY);
        }
    }

    /**
     * We assume we deal here with HTTP 400-type errors.
     * BUT, if there are more data in response body (like: JSON with
     * business error description), we may want to reinterpret error it is done
     * by interpretErrorResponse of GenericWebService subclass in
     * final error handler (like: in calling controller onBusinessError handler
     */
    private void handleErrorResponse(WebServiceResponse.Builder resBuilder, HttpResponse response, final String responseContentRaw, final int responseCode)
            throws IOException
    {
        ErrorType errType = ErrorType.parseInt(responseCode);

        if (errType == ErrorType.UNKNOWN) {
            errType = ErrorType.parseInt(((int) responseCode / 100) * 100);
        }

        boolean processed = false;

        try {

            if ((response != null) && (responseContentRaw.length() > 0)) {

                if (response.containsHeader("Content-Type")) {

                    Header type = response.getFirstHeader("Content-Type");

                    if (type.getValue().contains("json")) {

                        JSONObject responseContent = new JSONObject(responseContentRaw);
                        if (responseContent.has("error")) {
                            SystemError error = SystemError.fromWebServicePayload(responseCode, responseContent);
                            resBuilder.fail(error);
                            processed = true;
                        }
                    }
                }
            }

        } catch (JSONException e) {

            Log.v(TAG, "Cannot parse error response", e);

        } finally {

            if (!processed) {
                resBuilder.fail(new SystemError(errType));
                resBuilder.setContentStatus(ContentStatus.EMPTY);
            }

        }
    }


    /**
     * Retrieve HTTP Status Code from the response
     */
    private static int parseStatusCode(HttpResponse response)
    {
        String statusLine = response.getStatusLine().toString();
        String[] chunks = statusLine.split(" ");
        return Integer.parseInt(chunks[1]);
    }

    /**
     * Converts JSONObject to an Entity. Wraps a try catch block.
     */
    private static StringEntity convertToEntity(JSONObject jsonObject)
    {
        try {
            return new StringEntity(jsonObject.toString(), "UTF-8");
        } catch (UnsupportedCharsetException e1) {
            // The JVM doesn't support UTF-8. Likely to happen only
            // on some experimental JVM implementation. Oracle's JVM
            // and Android's Dalvik do support UTF-8.
            throw new AssertionError("UTF-8 not supported");
        }
    }

    /**
     * Cancel the request.
     */
    public void cancel()
    {
        synchronized (LOCK) {

            if (rawRequest != null) {
                rawRequest.abort();
            }
            cancelled = true;
        }
    }

}
