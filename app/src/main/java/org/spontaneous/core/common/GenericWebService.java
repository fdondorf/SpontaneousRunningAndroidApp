package org.spontaneous.core.common;

import android.os.Bundle;

/**
 * Abstract generic webservice. Has several general methods to set params of request.
 */
public abstract class GenericWebService {
    /** Tag for debugging */
    public static final String TAG = GenericWebService.class.getSimpleName();

    /** Request parameter */
    private final Bundle params = new Bundle();

    /**
     * Sets request parameter.
     *
     * @param key   The key
     * @param value String-Value for key
     */
    public void setParam(String key, String value)
    {
        params.putString(key, value);
    }

    public void setRequiredParams(WebServiceRequestConfig req, String[] innerKeys, String[] putKeys) throws SystemException
    {
        int keyCount = Math.min(innerKeys.length, putKeys.length);
        for (int i = 0; i < keyCount; i++) {
            final String innerKey = innerKeys[i];
            final String putKey = putKeys[i];
            if (params.containsKey(innerKey)) {
                req.setParam(putKey, params.getString(innerKey));
            } else {
                throw new SystemException("Required webservice key not found: " + innerKey + " (mapped to: " + putKey + ")");
            }
        }
    }

    public void setParams(WebServiceRequestConfig req, String[] innerKeys, String[] putKeys) throws SystemException
    {
        int keyCount = Math.min(innerKeys.length, putKeys.length);
        for (int i = 0; i < keyCount; i++) {
            final String innerKey = innerKeys[i];
            final String putKey = putKeys[i];
            if (params.containsKey(innerKey)) {
                req.setParam(putKey, params.getString(innerKey));
            }
        }
    }

    /**
     * Interprets webservice response.
     *
     * @param response Webservice response
     */
    public abstract void interpretResponse(WebServiceResponse response);

}
