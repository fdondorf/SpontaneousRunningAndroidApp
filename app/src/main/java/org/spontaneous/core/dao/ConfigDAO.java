package org.spontaneous.core.dao;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.core.api.SerializableJSON;
import org.spontaneous.utility.JSONHelper;

/**
 * Configuration properties of current logged-in user
 * including ParcelShop data
 *
 * @author Florian Dondorf
 */
public class ConfigDAO implements SerializableJSON<ConfigDAO> {

    private String serverUrlKey = "";

    private static final String TAG = ConfigDAO.class.getSimpleName();

    @Override
    public JSONObject storeInJSON() throws JSONException
    {
        JSONObject to = new JSONObject();

        JSONHelper.setStringIfNotEmpty(to, "serverUrlKey", serverUrlKey);

        return to;
    }

    @Override
    public ConfigDAO restoreFromJSON(JSONObject to) throws JSONException
    {
        serverUrlKey = JSONHelper.getStringFailsafe(to, "serverUrl", "");
        return this;
    }

    public String getServerUrlKey()
    {
        return serverUrlKey;
    }

    public void setServerUrlKey(String serverUrlKey)
    {
        this.serverUrlKey = serverUrlKey;
    }

}
