package org.spontaneous.core.crossdomain;

import android.content.Context;
import android.util.Log;

import org.spontaneous.core.common.Common.PrefDomain;
import org.spontaneous.core.common.Common.PrefParams;
import org.spontaneous.utility.DateTimeHelper;
import org.spontaneous.utility.StorageUtil;
import org.spontaneous.utility.provider.SharedPrefStorageProvider;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This singleton handles authentication process.
 * It is responsible for handling tokens (validation, expire check, persistence).
 *
 * @author Dominik Dzienia
 */
public enum Authentication {
    INSTANCE;

    public static final String TAG = Authentication.class.getSimpleName();

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";

    private String tokenBody = null;
    private Date tokenExpires = null;
    private SharedPrefStorageProvider nonCryptoStorage = null;

    private Authentication()
    {
    }

    /**
     * Checks if user is authenticated.
     *
     * @return <code>true</code>, if authentication was successful, <code>false</code> otherwise
     */
    public boolean isAuthenticated()
    {

        if ((tokenBody == null) || (UserInfo.INSTANCE.getUserInfo() == null)) {
            return false; // TODO PRODUCTION = false
        }

        return verifyToken();
    }

    /**
     * Checks if the user has one of the roles "ROLE_ADMIN" or "ROLE_SUPERADMIN"
     * @return True, if user has one of the roles above, false otherwise
     */
    public boolean isAdmin() {
        List<String> adminAuths = Arrays.asList(ROLE_ADMIN, ROLE_SUPERADMIN);
        List<String> authorities = UserInfo.INSTANCE.getUserInfo().getAuthorities();
        if (authorities != null && !authorities.isEmpty()) {
            for (String adminAuth : adminAuths) {
                if (authorities.contains(adminAuth)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getToken()
    {
        return tokenBody;
    }

    public void setContext(Context ctx)
    {
        restore();
        nonCryptoStorage = new SharedPrefStorageProvider();
        nonCryptoStorage.setContext(ctx);
    }

    public void updateToken(String tokenBody, Date tokenExpires)
    {
        this.tokenBody = tokenBody;
        if (tokenExpires == null) {
            this.tokenExpires = null;
        } else {
            this.tokenExpires = (Date) tokenExpires.clone();
        }
        store();
        verifyToken();
    }

    private boolean verifyToken()
    {
        if (this.tokenExpires == null) {
            Log.v(TAG, "Never expiring token is present");
            return true;
        }

        Date now = new Date();
        if (now.getTime() <= tokenExpires.getTime()) {
            Log.v(TAG, "Token is present and not expired");
            return true;
        } else {
            Log.w(TAG, "Token is present but had already expired");
            this.tokenExpires = null;
            this.tokenBody = null;
            store();
            return false;
        }
    }

    public void clearAuth()
    {
        Log.w(TAG, "Token cleared");
        this.tokenExpires = null;
        this.tokenBody = null;
        store();
    }

    private void store()
    {
        Map<String, String> toStore = new HashMap<>();
        toStore.put(PrefParams.TOKEN_BODY, (tokenBody == null) ? "" : tokenBody);
        toStore.put(PrefParams.TOKEN_EXPIRES, (tokenExpires == null) ? "" : DateTimeHelper.getIsoDateFormat().format(tokenExpires));
        StorageUtil.INSTANCE.store(PrefDomain.AUTHENTICATION, toStore);
    }

    /**
     * Restores authentication data.
     */
    private void restore()
    {
        Map<String, String> toRestore = new HashMap<>();
        toRestore.put(PrefParams.TOKEN_BODY, "");
        toRestore.put(PrefParams.TOKEN_EXPIRES, "");

        Map<String, String> restored = StorageUtil.INSTANCE.restore(PrefDomain.AUTHENTICATION, toRestore);

        if (restored == null) {
            return;
        }

        String tokenCandidate = restored.get(PrefParams.TOKEN_BODY);
        String tokenExpCandidate = restored.get(PrefParams.TOKEN_EXPIRES);
        this.tokenBody = tokenCandidate.equals("") ? null : tokenCandidate;
        try {
            this.tokenExpires = tokenExpCandidate.equals("") ? null : DateTimeHelper.getIsoDateFormat().parse(tokenExpCandidate);
        } catch (ParseException e) {
            this.tokenExpires = null;
        }
    }

    /**
     * Get last used (remembered) user name (login)
     *
     * @return Last user name or null when no valid user name was remembered.
     */
    public String getRememberedUserName()
    {
        if (nonCryptoStorage != null) {
            return nonCryptoStorage.restore(PrefDomain.USER_PREFERENCES, PrefParams.USER_NAME, null);
        } else {
            return null;
        }
    }

    /**
     * Remembers or clears last used user name
     *
     * @param userName last user name or null when there should be no remembered user name
     * @return true when saving of user name was successful, false otherwise
     */
    public boolean setRememberedUserName(String userName)
    {
        if (nonCryptoStorage != null) {
            return nonCryptoStorage.store(PrefDomain.USER_PREFERENCES, PrefParams.USER_NAME, userName);
        } else {
            return false;
        }
    }

    /**
     * Get info if user wants to stay logged in
     *
     * @return True, if user wants to stay logged in
     */
    public boolean isRememberLoggedIn()
    {
        if (nonCryptoStorage != null) {
            return nonCryptoStorage.restore(PrefDomain.USER_PREFERENCES, PrefParams.USER_STAY_LOGGED, false);
        } else {
            return false;
        }
    }

    /**
     * Remembers or clears if user wants to stay logged in
     *
     * @param stayLogged True, if user wants to stay logged, false otherwise
     * @return true when saving of value was successful, false otherwise
     */
    public boolean setRememberLogin(boolean stayLogged)
    {
        if (nonCryptoStorage != null) {
            return nonCryptoStorage.store(PrefDomain.USER_PREFERENCES, PrefParams.USER_STAY_LOGGED, stayLogged);
        } else {
            return false;
        }
    }

}
