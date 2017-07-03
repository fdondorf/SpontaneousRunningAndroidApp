package org.spontaneous.core.crossdomain;

import android.content.Context;

import org.spontaneous.utility.SecurityUtil;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Singleton centralizing configuration access (for configuration related constants,
 * user run time settings are handles by Preferences dialog).
 *
 * This implementation introduces configuration profiles and config resolving.
 * Configuration i based on Android String Resources, which are flat by nature
 * (only big set of key-values).
 *
 * To enable profiling, each configuration is placed in string by res/values/config.xml
 * file, following strict naming convention:
 *
 * "config_" + profiles + "_config_key"
 *
 * Profiles here are alphabetically sorted list of configuration profiles this
 * config belong.
 *
 * While config retrieval, algorithm first finds config entries that are most specific
 * (=matching largest set of profiles) than try to fall back to more generic ones.
 *
 * Current profile selection is decided by value of config_profile entry.
 *
 * For example, for profile set: [debug,internet,test] matching order of config
 * entries will be like:
 *
 * config_debug_internet_test_mysetting (best match)
 * config_debug_test_mysetting (two profiles matching)
 * config_internet_mysetting ("internet" is before "test" in alphabet order)
 * config_test_mysetting
 * config_mysetting (last, generic match)
 *
 * @author Dominik Dzienia
 */
public enum ConfigProvider {
    INSTANCE;

    private ConfigProvider()
    {
        //configSet = new ConfigSet();
    }


    private Context ctx;
    private String packageName;
    //public ConfigSet configSet;

    private TreeSet<String> profiles = new TreeSet<String>();
    private HashMap<String, String> configCache = new HashMap<String, String>();

    public void setContext(Context ctx)
    {
        this.ctx = ctx;
        this.packageName = ctx.getPackageName();
        //this.configSet.setContext(ctx);
    }

    public String getConfig(String key)
    {
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        }
        return getLowLevelConfig(key);
    }


    private String getLowLevelConfig(String key)
    {
        int resId = ctx.getResources().getIdentifier("config_" + key, "string", packageName);

        if (resId == 0) {
            return null;
        } else {
            return ctx.getString(resId);
        }
    }

    public String getAppKey()
    {
        return SecurityUtil.getAppKey(ctx);
    }

    /**
     * Gets current configuration profiles, default or derived from
     * user selected Config Set (possible only by DEBUG builds)
     */
    public String getCurrentConfigProfiles()
    {
        String userSelectedConfigSet = Authentication.INSTANCE.getConfigKey();
        if ((userSelectedConfigSet != null) && (!userSelectedConfigSet.equals(""))) {
            return userSelectedConfigSet;
        }
        return "";
    }

    /* ----- config sets ---------------------------------------------------- */

    public void cleanBakendRelatedSessionSettings()
    {
        Authentication.INSTANCE.clearAuth();
    }
}
