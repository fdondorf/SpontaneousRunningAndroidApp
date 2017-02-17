
package org.spontaneous.utility.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.spontaneous.core.StorageProvider;
import org.spontaneous.core.common.Common.PrefDomain;
import org.spontaneous.core.common.Common.PrefParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Secured version of storage provider, uses local private app data directory
 * and AES encryption.
 *
 * Be aware, that key for encryption is also stored in data directory file
 * and is available for potential attacker. So only security this provider
 * gives is to obfuscate data and protect against automated and tool attacks.
 *
 * Real file name (in private dir) is derived from domain and key, hashed with
 * SHA-256 to obfuscate meaning of file contents.
 *
 * @author Dominik Dzienia
 */
public class HardenedStorageProvider implements StorageProvider {
    public static final String TAG = HardenedStorageProvider.class.getSimpleName();

    private Context ctx;
    private volatile byte[] key = null;
    private InternalStorageProvider masterProvider = null;
    private Object syncPoint = new Object();
    private Object innerSyncPoint = new Object();

    @Override
    public void setContext(Context ctx)
    {
        this.ctx = ctx;
        this.masterProvider = new InternalStorageProvider();
        this.masterProvider.setContext(ctx);
    }

    public void ensureKey()
    {
        if (key == null) {

            synchronized (syncPoint) {

                if (key == null) {

                    if (masterProvider == null) {
                        throw new RuntimeException("MasterProvider not set - have you setContext?");
                    }

                    String rawHexKey = this.masterProvider.restore(PrefDomain.INTERNAL_KEY,
                            PrefParams.API, "");
                    if (rawHexKey.equals("")) {
                        synchronized (innerSyncPoint) {
                            rawHexKey = this.masterProvider.restore(PrefDomain.INTERNAL_KEY,
                                    PrefParams.API, "");
                            if (rawHexKey.equals("")) {
                                rawHexKey = buildRawHexKey();
                                this.masterProvider.store(PrefDomain.INTERNAL_KEY,
                                        PrefParams.API, rawHexKey);


                            }
                        }
                    }

                    key = decodeRawHexHey(rawHexKey);
                }
            }
        }

        Log.v(TAG, "SECRET KEY: " + Base64.encodeToString(key, Base64.NO_WRAP));
    }

    private byte[] decodeRawHexHey(String rawHexKey)
    {
        return Base64.decode(rawHexKey, Base64.NO_WRAP);
    }

    @SuppressLint("TrulyRandom")
    private String buildRawHexKey()
    {
        SecureRandom rand = new SecureRandom();
        byte[] someKey = new byte[32];
        rand.nextBytes(someKey);
        return Base64.encodeToString(someKey, Base64.NO_WRAP);
    }

    @Override
    public boolean store(String domain, String key, boolean value) {

        return false;
    }

    @Override
    public boolean restore(String domain, String key, boolean value) {

        return false;
    }

    @Override
    public boolean store(String domain, String key, String value)
    {
        Log.d(TAG, "STORE ---- " + domain + "::" + key);
        ensureKey();

        String fileName = makeFileName(domain, key);

        byte[] rawdata = getEncryptedBytes(value, fileName);

        if (rawdata == null) {
            return false;
        }

        FileOutputStream outputStream = null;

        try {

            outputStream = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(rawdata, 0, rawdata.length);
            Log.d(TAG, "STORED SIZE: " + rawdata.length + "B @ " + fileName);

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
            Log.e(TAG, "CHOSEN ENCODING IS NOT SUPORTED", e);
        }

        return domain + "__" + key;

    }

    @Override
    public String restore(String domain, String key, String defaultValue)
    {
        Log.d(TAG, "RESTORE ---- " + domain + "::" + key);
        ensureKey();

        int size = 0;
        String decrypted = null;

        try {
            byte[] savedData = null;

            String fileName = makeFileName(domain, key);
            File readFrom = new File(ctx.getFilesDir(), fileName);

            if (readFrom.exists()) {
                size = (int) readFrom.length();
            }

            if (size > 0) {
                Log.d(TAG, "RESTORED SIZE: " + size + "B @ " + fileName);

                FileInputStream inputStream = ctx.openFileInput(fileName);
                savedData = IOUtils.toByteArray(inputStream);
                inputStream.close();
                decrypted = getDecryptedString(savedData, fileName);
            }

            if (decrypted == null) {
                if (size > 0) {
                    ctx.deleteFile(fileName);
                    Log.d(TAG, "INVALID, deleted: " + fileName);
                }
                Log.d(TAG, "NOT RESTORED");
                return defaultValue;
            }

            Log.v(TAG, "RESTORED: " + decrypted);

            return decrypted;

        } catch (final Exception ex) {
            Log.e(TAG, "RESTORE: Exception while reading in saved data", ex);
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

    public byte[] getEncryptedBytes(String value, String fileName)
    {

        MessageDigest cript;
        try {
            cript = MessageDigest.getInstance("SHA-256");

            cript.reset();
            cript.update(fileName.getBytes("UTF8"));
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(cript.digest(),
                    0, 16));

            SecretKeySpec newKey = new SecretKeySpec(key, "AES");
            Cipher cipher = null;
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

            return cipher.doFinal(value.getBytes());

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "getEncryptedBytes", e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "getEncryptedBytes", e);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "getEncryptedBytes", e);
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "getEncryptedBytes", e);
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "getEncryptedBytes", e);
        } catch (BadPaddingException e) {
            Log.e(TAG, "getEncryptedBytes", e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getEncryptedBytes", e);
        }

        return null;

        // return value.getBytes();
    }

    public String getDecryptedString(byte[] savedData, String fileName)
    {
        if (savedData == null) {
            return null;
        }
        MessageDigest cript;
        try {

            cript = MessageDigest.getInstance("SHA-256");

            cript.reset();
            cript.update(fileName.getBytes());
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(cript.digest(),
                    0, 16));
            SecretKeySpec newKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
            return new String(cipher.doFinal(savedData), "UTF8");

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "getDecryptedString", e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "getDecryptedString", e);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "getDecryptedString", e);
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "getDecryptedString", e);
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "getDecryptedString", e);
        } catch (BadPaddingException e) {
            Log.e(TAG, "getDecryptedString", e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getDecryptedString", e);
        }

        return null;

    }

}
