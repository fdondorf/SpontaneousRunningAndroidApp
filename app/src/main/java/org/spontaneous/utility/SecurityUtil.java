
package org.spontaneous.utility;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;

import net.iharder.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAKey;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SecurityUtil {
    public static final String TAG = SecurityUtil.class.getSimpleName();

    public static String getX509ThumbPrint(X509Certificate cert)
    {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");    // NOSONAR squid:S2070 foreign system requires SHA-1
            byte[] der = cert.getEncoded();
            md.update(der);
            byte[] digest = md.digest();
            return hexify(digest, true);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Cannot check Certificate thumbprint, algorithm not supported", e);
            return "";
        } catch (CertificateEncodingException e) {
            Log.e(TAG,
                    "Cannot check Certificate thumbprint, something went wrong with certificate", e);
            return "";
        }

    }

    private static String hexify(byte bytes[], boolean forCert)
    {
        char[] hexDigits = {
                '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };

        StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            if ((forCert) && (i > 0)) {
                buf.append(" ");
            }
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

    public static String getAppKey(Context ctx)
    {
        return getGenKey(ctx, "raw.dat", "AK");
    }

    public static String getLoginAuthBasicBase(Context ctx)
    {
        return getGenKey(ctx, "oab.dat", "OAB");
    }

    public static String getGenKey(Context ctx, String entropy, String msg)
    {
        AssetManager assets = ctx.getAssets();
        InputStream myInput;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            myInput = assets.open(entropy);
            byte[] rawDat = readBytes(myInput);
            myInput.close();
            md.update(rawDat);
            byte[] digest = md.digest();
            return hexify(digest, false);
        } catch (IOException e) {
            Log.e(TAG, "Cannot get " + msg, e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Algorithm not supported with " + msg, e);
        }
        return "";
    }

    private static byte[] readBytes(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static String makeSHA2(String in)
    {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(in.getBytes());
            byte[] digest = md.digest();
            return hexify(digest, false);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Algorithm not supported with app key", e);
        }
        return "";
    }

    /**
     * Decodes raw binary DER data from PEM encoded key or certificate
     *
     * @param pemEncodedPayload PEM encoded key or certificate
     * @return binary (DER) contents of key or certificate
     */
    public static byte[] decodeDERfromPEM(String pemEncodedPayload)
    {
        final String PEMseparator = "-----";
        String normalisedPayload = pemEncodedPayload.replaceAll("\n", "");
        normalisedPayload = normalisedPayload.replaceAll("\r", "");
        if (normalisedPayload.contains(PEMseparator)) {
            String[] sections = normalisedPayload.split(PEMseparator);
            if (sections.length < 3) {
                Log.e(TAG, "Wrong PEM header");
                return new byte[0];
            }
            normalisedPayload = sections[2];
        }

        if (normalisedPayload.length() < 3) {
            Log.e(TAG, "Wrong PEM body");
            return new byte[0];
        }

        try {
            byte[] derBody = Base64.decode(normalisedPayload);
            return derBody;
        } catch (IOException e) {
            Log.e(TAG, "Wrong PEM body");
            return new byte[0];
        }
    }

    /**
     * Extracts public key from certificate
     *
     * @param pemEncodedCert PEM encoded X509 Certificate
     * @return Public key object
     */
    public static PublicKey getPublicKeyFromPEMCertificate(String pemEncodedCert)
    {
        byte[] DERCert = decodeDERfromPEM(pemEncodedCert);

        if (DERCert.length < 3) {
            Log.e(TAG, "Decoding PEM with certificate failed");
            return null;
        }

        PublicKey pk = null;
        InputStream certIn = null;

        try {
            certIn = new ByteArrayInputStream(DERCert);
            CertificateFactory f = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) f.generateCertificate(certIn);
            pk = certificate.getPublicKey();
        } catch (CertificateException e) {
            Log.e(TAG, "Cannot read public key from certificate", e);
        } finally {
            if (certIn != null) {
                try {
                    certIn.close();
                } catch (IOException e) {
                    Log.e(TAG, "Somehow closing stream failed", e);
                }
            }
        }

        return pk;
    }

    /**
     * Tries to determine size of RSA key, used to know encryptable block size
     *
     * @param key RSA key object
     * @return key size, in bytes
     */
    private static long getKeyBitSize(RSAKey key)
    {
        int bitLength = key.getModulus().bitLength();
        long refBitLength = 512;

        for (int i = 0; i < 7; i++) {
            if (bitLength <= refBitLength) {
                return refBitLength;
            }
            refBitLength = refBitLength << 1;
        }

        return refBitLength;
    }

    /**
     * Encrypts payload using RSA with PKCS1Padding
     * Only payloads shorter than key block size-12 are supported.
     *
     * @param data clear text to encrypt
     * @param key  key used for encryption
     * @return base64 encoded cipher text, or null on failure spontaneous-secret
     */
    public static String encrypt(String data, Key key)
    {
        if (key == null) {
            Log.e(TAG, "Cannot encrypt with null key");
            return null;
        }

        if (key instanceof RSAKey) {
            long maxPayloadSize = (getKeyBitSize((RSAKey) key) / 8) - 12;
            if (data.length() > maxPayloadSize) {
                Log.e(TAG, "Payload is too long to fit into block size allowed by chosen encryption algorithm");
                return null;
            }
        }

        Cipher c;
        try {
            c = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = c.doFinal(data.getBytes());
            String encodedString = Base64.encodeBytes(encrypted);
            return encodedString;

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Encryption: Algorithm not supported", e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "Encryption: Padding not supported", e);
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "Encryption: Wrong block size", e);
        } catch (BadPaddingException e) {
            Log.e(TAG, "Encryption: Padding error", e);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Encryption: Key error", e);
        }

        return null;
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if any of given packages is present (installed) on device
     *
     * @param ctx             Context, needed for package manager
     * @param packagesToCheck list of packages (fully qualified names) to check
     * @return <code>true</code> if at least one of specified packages is found,
     * <code>false</code> if none of packages is installed
     */
    private static boolean checkAnyOfPackagesInstalled(Context ctx, JSONArray packagesToCheck)
    {
        final PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Set<String> packages = new HashSet<>();

        for (ApplicationInfo packageInfo : apps) {
            packages.add(packageInfo.packageName);
        }

        for (int i = 0; i < packagesToCheck.length(); i++) {
            try {
                if (packages.contains(packagesToCheck.getString(i))) {
                    return true;
                }
            } catch (JSONException e) {
                continue;
            }
        }

        return false;
    }

    /**
     * File related condition checker
     */
    interface FileCondition {
        boolean meets(File pathToCheck);
    }

    /**
     * Checks if any of given file paths meets specified condition.
     *
     * @param pathsToCheck array of paths to check
     * @param condition    condition aganst we check paths
     * @return <code>true</code> as soon as there is path that meets condition,
     * <code>false</code> if all paths do not meet condiditon.
     */
    private static boolean checkAnyOfPathsMeetsCondition(JSONArray pathsToCheck, FileCondition condition)
    {
        for (int i = 0; i < pathsToCheck.length(); i++) {
            try {
                if (condition.meets(new File(pathsToCheck.getString(i)))) {
                    return true;
                }
            } catch (JSONException e) {
                continue;
            }
        }
        return false;
    }

    private static FileCondition existsCondition = new FileCondition() {
        @Override
        public boolean meets(File pathToCheck)
        {
            return pathToCheck.exists();
        }
    };

    private static FileCondition writableCondition = new FileCondition() {
        @Override
        public boolean meets(File pathToCheck)
        {
            return pathToCheck.canWrite();
        }
    };

    private static FileCondition readableCondition = new FileCondition() {
        @Override
        public boolean meets(File pathToCheck)
        {
            return pathToCheck.canRead();
        }
    };

    /**
     * Checks build tags flag
     *
     * @return <code>true</code> if it is test build (non-official one)
     */
    private static boolean checkAndroidBuildTags()
    {
        final String osBuildTags = android.os.Build.TAGS;
        return (osBuildTags != null) && (osBuildTags.contains("test-keys"));
    }

    /**
     * Protects JailBreak detection code from beeing clearly visible in sourcecode.
     * It is plain easy Base64 encoded JSON file
     * Take a look into /60_tools/app_security_base64 for converter and source JSON.
     *
     * @return JSONObject with jailbreak detection config.
     */
    private static JSONObject getSecurityCheckConfig()
    {
        try {
            return new JSONObject(
                    new String(Base64.decode(
                            "ewoJInBhY2thZ2VzIjogWwoJCSJjb20uYmlndGluY2FuLmFuZHJvaWQuYWRmcmVlIiwKCQkiY29tLmNn\n" +
                                    "b2xsbmVyLmZsYXNoaWZ5IiwNCiAgICAgICAgImNvbS5kZXZhZHZhbmNlLnJvb3RjbG9ha3BsdXMiLAoJ\n" +
                                    "CSJjb20uaGV4YW1vYi5ob3d0b3Jvb3QiLAogICAgICAgICJjb20ua291c2hpa2R1dHRhLnN1cGVydXNl\n" +
                                    "ciIsIAoJCSJjb20ubm9zaHVmb3UuYW5kcm9pZC5zdSIsIAoJCSJjb20ucmFtLm1lbW9yeS5ib29zdGVy\n" +
                                    "LmNwdS5zYXZlciIsCiAgICAgICAgImNvbS5yYW1kcm9pZC5hcHBxdWFyYW50aW5lIiwgCgkJImNvbS5y\n" +
                                    "b3RhcnloZWFydC5zdS5yb290LnRvb2xzIiwKICAgICAgICAiY29tLnRoaXJkcGFydHkuc3VwZXJ1c2Vy\n" +
                                    "IiwgCiAgICAgICAgImNvbS56YWNoc3BvbmcudGVtcHJvb3RyZW1vdmVqYiIsCiAgICAgICAgImRlLnJv\n" +
                                    "YnYuYW5kcm9pZC54cG9zZWQuaW5zdGFsbGVyIiwKICAgICAgICAiZXUuY2hhaW5maXJlLmZsYXNoIiwK\n" +
                                    "CQkiZXUuY2hhaW5maXJlLnN0aWNrbW91bnQiLAoJCSJldS5jaGFpbmZpcmUuc3VwZXJzdSIsIAoJCSJl\n" +
                                    "dS5jaGFpbmZpcmUuc3VwZXJzdS5wcm8iLAoJCSJzdGVyaWNzb24uYnVzeWJveCIsCiAgICAgICAgInJ1\n" +
                                    "Lm1lZWZpay5idXN5Ym94IgoJXSwKCSJmaWxlcyI6IFsNCgkJIi9kYXRhL2xvY2FsL2Jpbi9zdSIsIAoJ\n" +
                                    "CSIvZGF0YS9sb2NhbC9zdSIsIAoJCSIvZGF0YS9sb2NhbC94YmluL3N1IiwgCgkJIi9zYmluL3N1IiwK\n" +
                                    "CQkiL3N1L2Jpbi9zdSIsCgkJIi9zeXN0ZW0vYXBwL1N1cGVydXNlci5hcGsiLAoJCSIvc3lzdGVtL2Jp\n" +
                                    "bi8uZXh0Ly5zdSIsCgkJIi9zeXN0ZW0vYmluL3N1IiwKICAgICAgICAiL3N5c3RlbS9iaW4vZmFpbHNh\n" +
                                    "ZmUvc3UiLCAKCQkiL3N5c3RlbS9zZC94YmluL3N1IiwKCQkiL3N5c3RlbS9zdSIsCgkJIi9zeXN0ZW0v\n" +
                                    "dXNyL3dlLW5lZWQtcm9vdC9zdS1iYWNrdXAiLAoJCSIvc3lzdGVtL3hiaW4vbXUiLAoJCSIvc3lzdGVt\n" +
                                    "L3hiaW4vc3UiDQoJXSwKCSJ3cml0YWJsZSI6IFsKCQkiLyIsCgkJIi9kYXRhIiwKCQkiL2RldiIsCgkJ\n" +
                                    "Ii9ldGMiLAoJCSIvcHJvYyIsCgkJIi9zYmluIiwKCQkiL3N5cyIsCgkJIi9zeXN0ZW0iLAoJCSIvc3lz\n" +
                                    "dGVtL2JpbiIsCgkJIi9zeXN0ZW0vc2JpbiIsCgkJIi9zeXN0ZW0veGJpbiIsCgkJIi92ZW5kb3IvYmlu\n" +
                                    "IgoJXSwKCSJyZWFkYWJsZSI6IFsKCQkiL2RhdGEiCgldCn0=")
                    ));
        } catch (JSONException e) {
            Log.e(TAG, "Security config format error", e);
        } catch (IOException e) {
            Log.e(TAG, "Security config encoding error", e);
        }

        return null;
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if device is rooted or attempts was made to root it.
     * Note that there is no guarantee this procedure will always find root.
     * Also, it may generate false positives.
     *
     * @param ctx Android context
     * @return <code>true</code> if traces of rooting are detected.
     */
    public static boolean isRooted(Context ctx)
    {
        JSONObject config = getSecurityCheckConfig();
        if (config == null) {
            return false;
        }
        try {
            if (checkAndroidBuildTags()) {
                return true;
            }

            if (checkAnyOfPackagesInstalled(ctx, config.getJSONArray("packages"))) {
                return true;
            }

            if (checkAnyOfPathsMeetsCondition(config.getJSONArray("files"), existsCondition)) {
                return true;
            }

            if (checkAnyOfPathsMeetsCondition(config.getJSONArray("writable"), writableCondition)) {
                return true;
            }

            if (checkAnyOfPathsMeetsCondition(config.getJSONArray("readable"), readableCondition)) {
                return true;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Security config internal error", e);
        }

        return false;
    }


}
