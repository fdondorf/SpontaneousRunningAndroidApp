package org.spontaneous.core.ws.clients;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.spontaneous.core.common.WebServiceRequestConfig;
import org.spontaneous.utility.SecurityUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings("squid:S2068")    // Password is not hard-coded
public class HttpBasicAuthClient {

    public static final String TAG = HttpBasicAuthClient.class.getSimpleName();

    private String user = "";
    private String password = "";
    //protected String serverCertThumbprints = "";
    public HashMap<String, String> headers;

    public int responseCode = 0;
    public String responseText = "";
    private HttpRequestBase httpGet;

    // --------------------------------------------------------------------------

    public HttpBasicAuthClient()
    {
        headers = new HashMap<String, String>();
    }

    // --------------------------------------------------------------------------

    public synchronized HttpRequestBase getHttpRequest()
    {
        return httpGet;
    }

    private synchronized void setHttpRequest(HttpRequestBase httpGet)
    {
        this.httpGet = httpGet;
    }


    private static boolean serverThumprintIsWhitelisted(final String serverThumbprint,
                                                        final String comaSeparatedThumbprintWhitelist)
    {
        if (comaSeparatedThumbprintWhitelist == null) {
            return false;
        }
        final Locale loc = Locale.ENGLISH;
        final String serverThumbprintToCheck = serverThumbprint.toLowerCase(loc).replaceAll("[^a-zA-Z0-9]", "");

        String[] whitelist = comaSeparatedThumbprintWhitelist.split(",");
        for (String whitelistEntry : whitelist) {
            final String candidate = whitelistEntry.toLowerCase(loc).replaceAll("[^a-zA-Z0-9]", "");
            if (candidate.equals(serverThumbprintToCheck)) {
                return true;
            }
        }
        return false;
    }

    public static DefaultHttpClient createSSLClient(WebServiceRequestConfig reqCfg)
    {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

        final String expectedThumbPrints = reqCfg.getServerThumbprints();

        X509TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] xcs, String string)
                    throws CertificateException
            {
            }

            public void checkServerTrusted(X509Certificate[] xcs, String string)
                    throws CertificateException
            {

                final X509Certificate serverCert = xcs[0];
                X509Certificate verifyCert = serverCert;

                if (xcs.length > 1) {
                    final Principal issuerToFind = serverCert.getIssuerDN();
                    for (int a = 1; a < xcs.length; a++) {
                        if (xcs[a].getSubjectDN().equals(issuerToFind)) {
                            verifyCert = xcs[a];
                            break;
                        }
                    }
                }

                final String backendThumbprint = SecurityUtil.getX509ThumbPrint(verifyCert);
                if (!serverThumprintIsWhitelisted(backendThumbprint, expectedThumbPrints)) {
                    Log.e(TAG, "Wrong server certificate, expected one of : " + expectedThumbPrints + " thumbprint but got: " + backendThumbprint);
                    throw new CertificateException("Server Certificate not trusted!");
                }
            }

            public X509Certificate[] getAcceptedIssuers()
            {
                Log.e("aaa", "getAcceptedIssuers");
                return new X509Certificate[0];
            }
        };
        SSLContext ctx;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new TLSSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = httpClient.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, httpClient.getParams());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Unsupported TLS algorithm", e);
        } catch (KeyManagementException e) {
            Log.e(TAG, "TLS key management error", e);
        } catch (UnrecoverableKeyException e) {
            Log.e(TAG, "Problem while establishing TLS connection", e);
        } catch (KeyStoreException e) {
            Log.e(TAG, "KeyStore error while establishing TLS connection", e);
        }
        return null;
    }

    // --------------------------------------------------------------------------

    public String requestPost(String url, JSONObject jsonObject)
    {

        this.responseCode = 0;
        this.responseText = "";

        DefaultHttpClient httpClient = null;
        HttpResponse response = null;
        setHttpRequest(null);

        HttpParams httpParams = new BasicHttpParams();

        int connection_Timeout = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParams,
                connection_Timeout);
        HttpConnectionParams.setSoTimeout(httpParams, connection_Timeout);

        httpClient = new DefaultHttpClient(httpParams);

        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e1) {
            Log.e(TAG, "Unsuported encoding", e1);
        }
        entity.setContentType("application/json");
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        setHttpRequest(httpPost);
        try {

            CredentialsProvider credProvider = new BasicCredentialsProvider();
            credProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,
                    AuthScope.ANY_PORT), new UsernamePasswordCredentials(
                    this.user, this.password));

            for (String key : headers.keySet()) {
                getHttpRequest().setHeader(key, headers.get(key));
            }

            Log.i("HttpBasicAuthClient", "[request()] Requesting: " + url);

            response = httpClient.execute(getHttpRequest());

            String statusLine = response.getStatusLine().toString();
            String[] chunks = statusLine.split(" ");
            this.responseCode = Integer.parseInt(chunks[1]);

            this.responseText = EntityUtils.toString(response.getEntity());

        } catch (UnknownHostException e) {
            this.responseCode = 901;
            Log.e(TAG, "UnknownHostException", e);
        } catch (ConnectTimeoutException e) {
            this.responseCode = 902;
            Log.e(TAG, "ConnectTimeoutException", e);
        } catch (ClientProtocolException e) {
            this.responseCode = 910;
            Log.e(TAG, "ClientProtocolException", e);
        } catch (IOException e) {
            this.responseCode = 920;
            Log.e(TAG, "IOException", e);
        }

        return this.responseText;
    }

    // ==========================================================================
    // getters and setters
    // ==========================================================================

    public String getUser()
    {
        return user;
    }

    // --------------------------------------------------------------------------

    public void setUser(String user)
    {
        this.user = user;
    }

    // --------------------------------------------------------------------------

    public String getPassword()
    {
        return password;
    }

    // --------------------------------------------------------------------------

    public void setPassword(String password)
    {
        this.password = password;
    }

    // --------------------------------------------------------------------------
    /*
    public void setThumbprints(String serverCertThumbprints) {
        this.serverCertThumbprints = serverCertThumbprints;
    }
    
 // --------------------------------------------------------------------------

    public String getThumbprints() {
        return this.serverCertThumbprints;
    }*/

}
