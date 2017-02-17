package org.spontaneous.core.crossdomain;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.R;
import org.spontaneous.core.common.Common.PrefDomain;
import org.spontaneous.core.common.Common.PrefParams;
import org.spontaneous.core.common.CommonService;
import org.spontaneous.core.dao.UserDAO;
import org.spontaneous.utility.SecurityUtil;
import org.spontaneous.utility.StorageUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This singleton handles information about currently logged in user account,
 * including assigned Parcel Shop info.
 *
 * @author Dominik Dzienia
 */
public enum UserInfo {
    INSTANCE;

    public static final String TAG = Authentication.class.getSimpleName();

    private UserDAO userInfo = null;
    private Context ctx;

    private UserInfo()
    {

    }

    public void setContext(Context ctx)
    {
        this.ctx = ctx;
        restore();
        notifyPossibleUserChange();
    }

    private void store()
    {
        Map<String, String> toStore = new HashMap<>();
        try {
            toStore.put(PrefParams.USER_INFO, (userInfo == null) ? "" : userInfo.storeInJSON().toString());
        } catch (JSONException e) {
            Log.e(TAG, "Storing UserInfo failed", e);
        }
        StorageUtil.INSTANCE.store(PrefDomain.USER_INFO, toStore);
    }

    private void restore()
    {
        Map<String, String> toRestore = new HashMap<>();
        toRestore.put(PrefParams.USER_INFO, "");

        Map<String, String> restored = StorageUtil.INSTANCE.restore(PrefDomain.USER_INFO, toRestore);

        if (restored == null) {
            return;
        }

        String userInfoPayload = restored.get(PrefParams.USER_INFO);
        if (!userInfoPayload.equals("")) {
            try {
                JSONObject userInfoJSON = new JSONObject(userInfoPayload);
                userInfo = new UserDAO().restoreFromJSON(userInfoJSON);
            } catch (JSONException e) {
                Log.e(TAG, "ReStoring UserInfo failed", e);
            }
        }
    }

    public void setUserInfo(UserDAO userInfo)
    {
        this.userInfo = userInfo;
        store();

        notifyPossibleUserChange();
    }

    private void notifyPossibleUserChange()
    {
        Log.w(TAG, "####################### user changed!!!!");
        Intent new_intent = new Intent();
        new_intent.setAction(CommonService.BroadcastEndpoint.SERVICE);
        new_intent.putExtra(CommonService.BroadcastParam.USER_CHANGED, true);
        ctx.sendBroadcast(new_intent);
    }

    public UserDAO getUserInfo()
    {
        return userInfo;
    }

    public boolean hasUserInfo()
    {
        return ((userInfo != null));
    }

    public String getFullUserName()
    {
        if ((userInfo != null) && ((userInfo.getLastName() != null) || (userInfo.getFirstName() != null))) {
            StringBuilder fullName = new StringBuilder();
            if (userInfo.getFirstName() != null) {
                fullName.append(userInfo.getFirstName().trim());
            }
            if (userInfo.getLastName() != null) {
                if ((userInfo.getLastName().trim().length() > 0) && (fullName.length() > 0)) {
                    fullName.append(" ");
                }
                fullName.append(userInfo.getLastName().trim());
            }

            return fullName.toString();
        } else {
            return ctx.getString(R.string.label_your_full_name);
        }
    }

    public String getCurrentUserUID()
    {
        String idBase = "";

        if (userInfo != null) {
            idBase += userInfo.getEmail();
            if (userInfo.getFirstName() != null) {
                idBase += "|" + userInfo.getFirstName();
            }
        } else {
            idBase = "default";
        }

        return SecurityUtil.makeSHA2(idBase);
    }

    /*
    public boolean isNewAppVersionAvailable()
    {
        if (userInfo != null) {
            final ConfigDAO config = userInfo.getConfig();
            if (config != null) {
                String newAppVersion = config.getNewerAppVersionAvailable();
                return newAppVersion != null && !newAppVersion.isEmpty();
            }
        }

        return false;
    }
    */
}
