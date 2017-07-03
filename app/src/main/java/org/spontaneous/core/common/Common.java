package org.spontaneous.core.common;

import android.content.Context;

import org.spontaneous.activities.MainActivity;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.ConfigProvider;
import org.spontaneous.core.crossdomain.UserInfo;
import org.spontaneous.utility.LanguageUtil;
import org.spontaneous.utility.StorageUtil;

/**
 * App wide methods and common constants (basically used as Intent/Storage keys).
 * For clarity, constants are categorized in static classes (Android convention
 * borrowed from R class):
 * <ul>
 * <li>PrefDomain - business domains (namespaces) of preferences, used with key-value storages</li>
 * <li>PrefParams - keys used with key-value storages</li>
 * <li>CallSource - definitions of Activity callers, used when calling Activities for result</li>
 * <li>CallResult - possible common Activity call results</li>
 * <li>TabSet - identifies Tab hosts, allows tab group identification for tab state persistence</li>
 * <li>IntentParam - names of parameters passed by Intent</li>
 * <li>ActivityState - keys used to persist specific activity state values</li>
 * </ul>
 *
 * @author Dominik Dzienia
 */
public class Common {

    public static final String BASE = MainActivity.class.getPackage().getName();

    // preferences

    public static final class PrefDomain {
        public static final String MAIN_PREFERENCES = Common.BASE + ".pref";
        public static final String USER_PREFERENCES = Common.BASE + ".user";
        public static final String FLAGS = Common.BASE + ".flag";
        public static final String AUTHENTICATION = Common.BASE + ".auth";
        public static final String USER_INFO = Common.BASE + ".userinfo";
        public static final String SYNC_KEY = Common.BASE + ".sync";
        public static final String INVENTORY = Common.BASE + ".inventory";
        public static final String INTERNAL_KEY = Common.BASE + ".common";
        public static final String LIFECYCLE = Common.BASE + ".lifecycle";
        public static final String LIST_STATE = Common.BASE + ".list.state";
        public static final String PREPLIST_PEP = Common.BASE + ".preplist.pep";
        public static final String PREPLIST_EXPRESS = Common.BASE + ".preplist.express";
    }

    public static final class PrefParams {
        public static final String TOKEN_BODY = Common.BASE + ".token_body";
        public static final String TOKEN_EXPIRES = Common.BASE + ".token_expires";
        public static final String USER_INFO = Common.BASE + ".user_info";
        public static final String SYNC_PAYLOAD = Common.BASE + ".sync_payload";
        public static final String SYNC_VERSION = Common.BASE + ".sync_format_version";
        public static final String INVENTORY_PAYLOAD = Common.BASE + ".sync_payload";
        public static final String INVENTORY_VERSION = Common.BASE + ".sync_format_version";
        public static final String API = Common.BASE + ".api"; // master key
        public static final String USER_CONFIGSET = Common.BASE + ".user_configset";
        public static final String GOING_HOME = Common.BASE + ".going_home";
        public static final String GOING_SCANNER = Common.BASE + ".going_scanner";
        public static final String LIST_INVENTORY = Common.BASE + ".list.inventory";
        public static final String USER_NAME = Common.BASE + ".user_name";
        public static final String USER_STAY_LOGGED = Common.BASE + ".user_stay_logged";
        public static final String BUILD_NUMBER = Common.BASE + ".build_number";
        public static final String CONFIG_KEY = Common.BASE + ".user_config_key";
    }

    public static final class Flags {
        /** Set when user was notified that device probably is rooted */
        public static final String USER_NOTIFIED_ROOT = Common.BASE + ".root_notified";
    }

    /**
     * Constants defining preference fields in setting dialog
     * but also corresponding Shared Preferences keys.
     */
    public static final class PreferenceFields {
        public static final String LANGUAGE = "language";
        public static final String SCANNER = "scanner";
        public static final String ABOUT = "about";
        public static final String VERSION_NUMBER = "version1";
        public static final String VERSION_BUILD = "version2";
        public static final String CONFIG_SET = "configset";
    }

    /**
     * Default values for preferences.
     */
    public static final class PreferenceFieldsDefaults {
        public static final String SCANNER = "auto";
    }

    // intents and activity params

    public static final class CallSource {
        public static final int DOES_NOT_MATTER = 0;
        public static final int SCANNER_DIALOG = 10;
        public static final int DIALOG_SCAN_ADD_UNKNOWN_PARCEL = 11;
        public static final int DIALOG_SCAN_ADD_OTC_PARCEL = 12;

        /** Initialisation of scanner dialog */
        public static final int SCANNER_DIALOG_INIT_FAILED = 15;

        /** Initialisation of scanner dialog */
        public static final int SCANNER_DIALOG_PERMISSIONS = 17;

        public static final int DELETE_ACTION = 100;
        public static final int MARK_ACTION = 110;
        public static final int MANUAL_ENTRY_ACTION = 150;
        public static final int LIST_EDIT_ACTION = 160;
        public static final int UNANNOUNCED_QUESTION_ACTION = 170;
        /** used for dialog indicating that parcel ID is incorrect and cannot be further processed */
        public static final int REJECTED_PARCEL_ID_ACTION = 175;
        public static final int VENTURE_DIALOG = 180;
        public static final int FULLSCREEN_ACTION = 200;
        public static final int BADGE_SCAN_ACTION = 300;
        public static final int LOGIN_ACTION = 1000;
        public static final int LOGOUT_ACTION = 1100;
        public static final int PARCEL_LIST_ACTION = 1200;
        public static final int APP_UPDATE_ACTION = 1300;
        public static final int UNPREPARED_PARCELS_ACTION = 1400;
        /** Used for dialog indicating that a parcel is overdue. */
        public static final int OVERDUE_PARCEL_ACTION = 1500;

        /** Used for dialog indicating that parcel should be marked as missing. */
        public static final int MARK_AS_MISSING_ACTION = 1600;

        /** Used for dialog indicating that a missed parcel has been scanned. */
        public static final int PARCEL_FOUND_ACTION = 1700;

        /** Used if user finishes scanning but there are still parcels of same consignee left. */
        public static final int HAND_OUT_RELEVANT_PARCELS_LEFT = 1800;

        /** Used for dialog for manual CoD amount entry. */
        public static final int COD_MANUAL_ENTRY_ACTION = 1850;

        /** Used for the phone number validation in the hand in screen */
        public static final int PHONE_NUMBER_VALIDATION_PROMPT = 1900;

        /** Used for ensuring that COD cash has been collected */
        public static final int COD_AMOUNT_COLLECTED = 2000;

        /** Used for checking if the user accepts the cash handover to the courier */
        public static final int COD_CASH_COURIER_PICKUP = 2100;

        /** Used for handling warning message after courier delivery scan. */
        public static final int COURIER_DELIVERY_VALIDATION_FAILED_WARNING_PROMPT = 2200;

        /** Used to handle prompts related to presence and update/install of Google Play Services */
        public static final int PLAY_SERVICES_HELPER = 3000;

        /** Google Play Services given provider for resolution of error */
        public static final int PLAY_SERVICES_RESOLUTION_PROVIDER = 3010;

        /** Rooted device detected */
        public static final int APP_ROOT_DETECTED = 9000;
    }

    public static final class CallResult {
        public static final int OK = 0;
        public static final int FAILED = 1;
        public static final int CONFIRM = 100;
        public static final int CANCEL = 200;
        public static final int GO_SCANNER = 301;
        public static final int YES = 400;
        public static final int NO = 500;
    }

    public static final class TabSet {
        public static final String CONFIRM_HANDIN_MAIN = Common.BASE + ".confirm.handin.main";
        public static final String CONFIRM_HANDOUT_MAIN = Common.BASE + ".confirm.handout.main";
        public static final String CONFIRM_HANDOUT_REFUSE = Common.BASE + ".confirm.handout.reject";
    }

    public static final class IntentParam {
        public static final String ACTION_KIND = Common.BASE + ".action_kind";
        public static final String SIGNATURE = Common.BASE + ".signature";
        public static final String DIALOG_LIST_MODE = Common.BASE + ".dialog_config";
        public static final String ALLOW_MARKING = Common.BASE + ".allow_marking";
        public static final String SHOW_PENDING = Common.BASE + ".show_pending";
        public static final String CUSTOM_PARCEL_ID = Common.BASE + ".parcel_id";
        public static final String PARCEL_INFO_SOURCE = Common.BASE + ".parcel_info_source";
        public static final String PARCEL_ID = Common.BASE + ".parcel_id";
        public static final String BADGE_ID = Common.BASE + ".badge_id";
        public static final String COD_AMOUNT = Common.BASE + ".cod_amount";
        public static final String LOCK_PORTRAIT_MODE = Common.BASE + ".lock_portrait_mode";
        public static final String TAB_PRESELECTION = Common.BASE + ".tab_preselection";
        public static final String HIDE_TAB_BAR = Common.BASE + ".hide_tab_bar";
    }

    public static final class IntentListModes {
        public static final String LIST_EDIT = Common.BASE + ".list_edit";
        public static final String LIST_CONFIRM = Common.BASE + ".list_confirm";
        public static final String LIST_PENDING = Common.BASE + ".list_pending";
    }

    public static final class ActivityState {
        public static final String LIST_SELECTED = Common.BASE + ".selected";
        public static final String CONFIRM_REQUEST_COUNTDOWN = Common.BASE + ".confirm.countdown";
        public static final String CONFIRM_SIGNATURE = Common.BASE + ".confirm.signature";
        public static final String EDITS_BASE = Common.BASE + ".editview.";
    }

    // common constants

    public static final String OK = "ok";
    public static final String ERROR = "error";

    /**
     * We need to provide global singletons with context to allow them to operate
     * on common Android resources (like strings constants, Shared Preferences).
     *
     * But enum-based singletons are constructed by class loader, when no context
     * is known/accessible (whole process is automated and not easily customizable).
     *
     * It is crude but works. Just ensure that this method is called first before
     * singleton usage (like: on main dialog onCreate or at service creation).
     * Each singleton internally checks if context was set before using it.
     *
     * @param ctx Application or local (Activity, Service) context.
     */
    public static void ensureContextForStaticManagers(Context ctx)
    {
        LanguageUtil.INSTANCE.setContext(ctx);
        StorageUtil.INSTANCE.setContext(ctx);
        Authentication.INSTANCE.setContext(ctx);
        UserInfo.INSTANCE.setContext(ctx);
        ConfigProvider.INSTANCE.setContext(ctx);
    }

    public static final int MINUTE_AS_SECONDS = 60;
    public static final int SECOND_AS_MILISECONDS = 1000;
    public static final int HOUR_AS_MINUTES = 60;
    public static final int HOUR_AS_SECONDS = HOUR_AS_MINUTES * MINUTE_AS_SECONDS;

    public static final String LOCAL_BROADCAST_PERMISSION = "com.dhl.dhlpaketshop.intercom";

}
