
package org.spontaneous.utility;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;
import android.util.Log;

import org.spontaneous.core.StorageProvider;
import org.spontaneous.core.common.Common;
import org.spontaneous.utility.provider.HardenedStorageProvider;
import org.spontaneous.utility.provider.SharedPrefStorageProvider;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This is a singleton helper class for storing and restoring data in a simple
 * key-value manner, with both key and value of String type. Works as proxy
 * which delegates storing and restoring to one of available StorageProvers
 * (like: HardenedStorageProvider). If other data type need to be used as value,
 * it should be serialized or deserialized to String (like, using JSON with
 * SerializableJSON interface) Class methods use common convention of argument
 * naming:
 * <ul>
 * <li>domain: String identifying namespace or business area</li>
 * <li>key: key used to identify variable.</li>
 * <li>value: variable value</li>
 * <li>defaultValue: used when storage does not contain given key</li>
 * </ul>
 * Additionally, to speed up retrieval on some StorageProviders, class provides
 * methods to batch retrieval/store of variables, with Maps instead of single
 * keys and values.
 *
 * @author Dominik Dzienia
 * @see org.spontaneous.core.StorageProvider
 * @see org.spontaneous.utility.provider.HardenedStorageProvider
 */
public enum StorageUtil {
    INSTANCE;

    public static final String TAG = StorageUtil.class.getSimpleName();

    final private StorageProvider provider;
    final private SharedPrefStorageProvider flagProvider;
    private Context ctx = null;

    private StorageUtil()
    {
        provider = new HardenedStorageProvider();
        if (ctx != null) {
            provider.setContext(ctx);
        }
        flagProvider = new SharedPrefStorageProvider();
        if (ctx != null) {
            flagProvider.setContext(ctx);
        }
    }

    public void setContext(Context ctx)
    {
        if (this.ctx == null) {
            this.ctx = ctx;
        }
        if (provider != null) {
            provider.setContext(ctx);
        }
        if (flagProvider != null) {
            flagProvider.setContext(ctx);
        }
    }

    public boolean store(String domain, String key, String value)
    {
        if (ctx != null) {
            return provider.store(domain, key, value);
        } else {
            return false;
        }
    }

    public boolean store(String domain, Map<String, String> turples)
    {
        if (ctx != null) {
            return provider.store(domain, turples);
        } else {
            return false;
        }
    }

    public String restore(String domain, String key, String defaultValue)
    {
        if (ctx != null) {
            return provider.restore(domain, key, defaultValue);
        } else {
            return null;
        }
    }

    private String serializeBundle(final Bundle bundle)
    {
        String base64 = null;
        final Parcel parcel = Parcel.obtain();
        try {
            parcel.writeBundle(bundle);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            zos.write(parcel.marshall());
            zos.close();
            base64 = Base64.encodeToString(bos.toByteArray(), 0);
            Log.i("STORAGE", base64);
        } catch (IOException e) {
            Log.e(TAG, "serializeBundle failed", e);
            base64 = null;
        } finally {
            parcel.recycle();
        }
        return base64;
    }

    private Bundle deserializeBundle(final String base64)
    {
        Bundle bundle = null;
        final Parcel parcel = Parcel.obtain();
        try {
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64, 0)));
            int len = 0;
            while ((len = zis.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            zis.close();
            parcel.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
            parcel.setDataPosition(0);
            bundle = parcel.readBundle();
        } catch (IOException e) {
            Log.e(TAG, "deserializeBundle failed", e);
            bundle = null;
        } finally {
            parcel.recycle();
        }

        return bundle;
    }

    public boolean storeBundle(String domain, String key, Bundle bundle)
    {
        if (ctx != null) {
            String serializedBundle = serializeBundle(bundle);
            if (serializedBundle != null) {
                store(domain, key, serializedBundle);
                return true;
            }
        }

        return false;
    }

    public Bundle restoreBundle(String domain, String key, Bundle defaultBundle)
    {
        Bundle bundle = defaultBundle;
        if (ctx != null) {
            String serializedBundle = provider.restore(domain, key, "");
            if (!serializedBundle.isEmpty()) {

                Bundle deserializedBundle = deserializeBundle(serializedBundle);
                if (deserializedBundle != null) {
                    bundle = deserializedBundle;
                }
            }
        }

        return bundle;
    }

    public String restore(String domain, String key)
    {
        return restore(domain, key, null);
    }

    public Map<String, String> restore(String domain, Map<String, String> turples)
    {
        if (ctx != null) {
            return provider.restore(domain, turples);
        } else {
            return null;
        }
    }

    public String getAssetsFileContents(String fileName)
    {
        if (ctx != null) {
            StringBuilder buf = new StringBuilder();
            try {
                InputStream is = ctx.getAssets().open(fileName);
                BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String str;
                while ((str = in.readLine()) != null) {
                    buf.append(str);
                }
                in.close();
                is.close();

                return buf.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error while reading assets file.", e);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Checks if the app has been updated and clears the cache if an update was detected.
     */
    public static void clearCacheIfNeeded(Context context)
    {
        try {
            String storedBuildNumber = StorageUtil.INSTANCE.restore(Common.PrefDomain.INTERNAL_KEY, Common.PrefParams.BUILD_NUMBER);
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int currentVersionCode = pInfo.versionCode;

            if (storedBuildNumber != null) {
                if (storedBuildNumber != null && currentVersionCode > Integer.parseInt(storedBuildNumber)) {
                    Log.d(TAG, "App has been updated. Old: " + storedBuildNumber + ", new: " + currentVersionCode);
                    clearCacheOnAppUpdate(context, String.valueOf(currentVersionCode));
                }
            } else {
                Log.w(TAG, "No build number found, clearing cache anyway.");
                clearCacheOnAppUpdate(context, String.valueOf(currentVersionCode));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Error while getting version code.");
        }
    }

    private static void clearCacheOnAppUpdate(Context context, String buildNumber)
    {
        clearCache(context);
        StorageUtil.INSTANCE.store(Common.PrefDomain.INTERNAL_KEY, Common.PrefParams.BUILD_NUMBER, String.valueOf(buildNumber));
    }

    private static void clearCache(Context context)
    {
        File dir = context.getCacheDir();
        if (dir != null && dir.isDirectory()) {
            deleteDir(dir);
        }
    }

    private static boolean deleteDir(File dir)
    {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        if (dir != null) {
            return dir.delete();
        } else {
            return false;
        }
    }

    /**
     * Checks if flag was set.
     * Flags are persistent, cleared by reinstall / new installation of app.
     *
     * @param flagName name of flag
     * @return <code>true</code> if flag is set
     */
    public boolean isFlagSet(String flagName)
    {
        return Boolean.parseBoolean(flagProvider.restore(Common.PrefDomain.FLAGS, flagName, "false"));
    }

    /**
     * Sets (turn on) flag
     * Flags are persistent, cleared by reinstall / new installation of app.
     *
     * @param flagName flag name to sets.
     */
    public void setFlag(String flagName)
    {
        flagProvider.store(Common.PrefDomain.FLAGS, flagName, "true");
    }

    /**
     * Turn off flag
     * Flags are persistent, cleared by reinstall / new installation of app.
     *
     * @param flagName flag name to sets.
     */
    public void unsetFlag(String flagName)
    {
        flagProvider.store(Common.PrefDomain.FLAGS, flagName, "false");
    }

}
