package org.spontaneous.utility.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.spontaneous.core.StorageProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements storage using Android Shared Preferences.
 *
 * @author Florian Dondorf
 */
public class SharedPrefStorageProvider implements StorageProvider {
    private Context ctx;

    @Override
    public void setContext(Context ctx)
    {
        this.ctx = ctx;
    }

    @Override
    public boolean store(String domain, String key, String value)
    {
        SharedPreferences settings = ctx.getSharedPreferences(domain, Context.MODE_PRIVATE);
        Editor edit = settings.edit();
        edit.putString(key, value);
        edit.apply();

        return true;
    }

    @Override
    public boolean store(String domain, String key, boolean value)
    {
        SharedPreferences settings = ctx.getSharedPreferences(domain, Context.MODE_PRIVATE);
        Editor edit = settings.edit();
        edit.putBoolean(key, value);
        edit.apply();

        return true;
    }

    @Override
    public String restore(String domain, String key, String defaultValue)
    {
        SharedPreferences settings = ctx.getSharedPreferences(domain, Context.MODE_PRIVATE);
        return settings.getString(key, defaultValue);
    }

    @Override
    public boolean restore(String domain, String key, boolean defaultValue)
    {
        SharedPreferences settings = ctx.getSharedPreferences(domain, Context.MODE_PRIVATE);
        return settings.getBoolean(key, defaultValue);
    }

    @Override
    public boolean store(String domain, Map<String, String> turples)
    {
        SharedPreferences settings = ctx.getSharedPreferences(domain, Context.MODE_PRIVATE);
        Editor edit = settings.edit();
        for (Map.Entry<String, String> turple : turples.entrySet()) {
            edit.putString(turple.getKey(), turple.getValue());
        }
        edit.apply();
        return true;
    }

    @Override
    public Map<String, String> restore(String domain, Map<String, String> turplesWithDefaults)
    {
        Map<String, String> restored = new HashMap<String, String>();
        SharedPreferences settings = ctx.getSharedPreferences(domain, Context.MODE_PRIVATE);
        for (Map.Entry<String, String> turple : turplesWithDefaults.entrySet()) {
            restored.put(turple.getKey(), settings.getString(turple.getKey(), turple.getValue()));
        }
        return restored;
    }

}
