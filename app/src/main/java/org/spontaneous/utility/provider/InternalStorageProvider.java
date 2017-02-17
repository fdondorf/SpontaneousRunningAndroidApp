
package org.spontaneous.utility.provider;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.spontaneous.core.StorageProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * File based storage provider, uses local private app data directory
 * but without encryption. Used primarily for storing HardenedStorage main key.
 *
 * Real file name (in private dir) is derived from domain and key, hashed with
 * SHA-256 to obfuscate meaning of file contents.
 *
 * @author Dominik Dzienia
 */
public class InternalStorageProvider implements StorageProvider {

    public static final String TAG = InternalStorageProvider.class.getSimpleName();
    private Context ctx;

    @Override
    public void setContext(Context ctx)
    {
        this.ctx = ctx;
    }

    @Override
    public boolean store(String domain, String key, String value)
    {
        String fileName = makeFileName(domain, key);

        byte[] rawdata = null;
        try {
            rawdata = value.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "CHOSEN ENCODING IS NOT SUPORTED", e);
        }

        if (rawdata == null) {
            return false;
        }

        FileOutputStream outputStream = null;

        try {

            outputStream = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(rawdata, 0, rawdata.length);

        } catch (IOException e) {
            Log.e(TAG, "SOMETHING WENT WRONG DURING WRITING INTO outputStream IN store()", e);

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "COULD NOT CLOSE outputStream IN store()", e);
                }
            }
        }

        return true;
    }

    @Override
    public boolean store(String domain, String key, boolean value) {

        return false;
    }

    @Override
    public boolean restore(String domain, String key, boolean value) {

        return false;
    }

    public String makeFileName(String domain, String key)
    {

        MessageDigest cript;

        try {
            cript = MessageDigest.getInstance("SHA-256");

            cript.reset();
            cript.update(new String(domain + "__" + key).getBytes());
            return new String(Base64.encode(cript.digest(), Base64.NO_WRAP | Base64.NO_PADDING
                    | Base64.URL_SAFE), "UTF8");

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "COULD NOT ENCODE FILENAME IN makeFileName()", e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "CHOSEN ENCODING IS NOT SUPPORTED", e);
        }

        return domain + "__" + key;

    }

    @Override
    public String restore(String domain, String key, String defaultValue)
    {
        try {
            byte[] savedData = null;

            String fileName = makeFileName(domain, key);
            File readFrom = new File(ctx.getFilesDir(), fileName);
            String loaded = null;

            int size = 0;

            if (readFrom.exists()) {
                size = (int) readFrom.length();
            }

            if (size > 0) {
                FileInputStream inputStream = ctx.openFileInput(fileName);
                savedData = IOUtils.toByteArray(inputStream);
                inputStream.close();
                if (savedData.length > 0) {
                    loaded = new String(savedData, "UTF8");
                }
            }

            if (loaded == null) {
                return defaultValue;
            }

            return loaded;

        } catch (final Exception ex) {
            Log.e("RESTORE", "Exception while reading in saved data", ex);
        }

        return defaultValue;
    }

    @Override
    public boolean store(String domain, Map<String, String> turples)
    {
        for (Map.Entry<String, String> turple : turples.entrySet()) {
            store(domain, turple.getKey(), turple.getValue());
        }
        return true;
    }

    @Override
    public Map<String, String> restore(String domain, Map<String, String> turplesWithDefaults)
    {
        Map<String, String> restored = new HashMap<String, String>();
        for (Map.Entry<String, String> turple : turplesWithDefaults.entrySet()) {
            final String restoredValue = restore(domain, turple.getKey(), turple.getValue());
            restored.put(turple.getKey(), restoredValue);
        }
        return restored;
    }

}
