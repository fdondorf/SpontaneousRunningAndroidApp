package org.spontaneous.utility;

import android.util.Log;
import android.util.SparseBooleanArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONHelper {
    public static final String TAG = JSONHelper.class.getSimpleName();

    public static JSONObject failureBuilder(int failureKind)
    {
        try {
            final JSONObject failureObj = new JSONObject("{'status':'failure','failure':" + failureKind + "}");
            return failureObj;
        } catch (JSONException e) {
            return null;
        }
    }

    public static String getStringFailsafe(JSONObject base, String key, String fallback)
    {
        if (base == null) {
            return fallback;
        }
        try {
            if (base.has(key)) {
                return base.getString(key);
            } else {
                return fallback;
            }
        } catch (JSONException e) {
            return fallback;
        }
    }

    public static List<String> getStringArrayFailsafe(JSONObject base, String key, List<String> fallback)
    {
        if (base == null) {
            return fallback;
        }
        try {
            if (base.has(key)) {
                List<String> result = new ArrayList<String>();
                JSONArray jsonArray = base.getJSONArray(key);
                for (int i = 0; i < jsonArray.length(); i++) {
                    //JSONObject jsonObj = jsonArray.getJSONObject(i);
                    result.add(jsonArray.getString(i));
                }
                return result;
            } else {
                return fallback;
            }
        } catch (JSONException e) {
            return fallback;
        }
    }

    public static boolean getBooleanFailsafe(JSONObject base, String key, boolean fallback)
    {
        if (base == null) {
            return fallback;
        }
        try {
            return base.getBoolean(key);
        } catch (JSONException e) {
            return fallback;
        }
    }

    public static int getIntFailsafe(JSONObject base, String key, int fallback)
    {
        if (base == null) {
            return fallback;
        }
        try {
            return base.getInt(key);
        } catch (JSONException e) {
            return fallback;
        }
    }

    public static Long getLongFailsafe(JSONObject base, String key, Long fallback)
    {
        if (base == null) {
            return fallback;
        }
        try {
            return base.getLong(key);
        } catch (JSONException e) {
            return fallback;
        }
    }

    public static void setStringIfNotEmpty(JSONObject target, String key, String data)
    {
        try {
            if ((data != null) && (!data.equals(""))) {
                target.put( key, data);
            }
        } catch (JSONException e) {
            Log.w(TAG, "Cannot set json field " + key, e);
        }
    }


    public static void setStringArrayIfNotEmpty(JSONObject target, String key, List<String> data)
    {
        try {
            if ((data != null) && (!data.isEmpty())) {
                JSONArray jArray = new JSONArray();
                int i = 1;
                for (String role : data) {
                    //JSONObject json = new JSONObject();
                    //json.put(Integer.toString(i), role);
                    jArray.put(role);
                    //jArray.put(json);
                    i++;
                }
                target.put(key, jArray);
            }
        } catch (JSONException e) {
            Log.w(TAG, "Cannot set json field " + key, e);
        }
    }

    public static void setLongIfNotEmpty(JSONObject target, String key, Long data)
    {
        try {
            if ((data != null)) {
                target.put( key, data);
            }
        } catch (JSONException e) {
            Log.w(TAG, "Cannot set json field " + key, e);
        }
    }

    public static void setBooleanIfNotEmpty(JSONObject target, String key, Boolean data)
    {
        try {
            if ((data != null)) {
                target.put( key, data);
            }
        } catch (JSONException e) {
            Log.w(TAG, "Cannot set json field " + key, e);
        }
    }

    public static SparseBooleanArray sparseBooleanArrayFromString(String payload)
    {
        try {
            final SparseBooleanArray sparse = new SparseBooleanArray();
            JSONArray arr = new JSONArray(payload);
            int len = arr.length();
            for (int i = 0; i < len; i++) {
                sparse.put(arr.getInt(i), true);
            }
            return sparse;
        } catch (JSONException e) {
            return new SparseBooleanArray();
        }
    }

}
