
package org.spontaneous.utility;

import android.content.Context;
import android.util.Log;

import org.spontaneous.core.common.error.ErrorMappings;
import org.spontaneous.core.common.error.ErrorType;
import org.spontaneous.core.common.error.SystemError;

import java.util.Locale;


public enum LanguageUtil {
    INSTANCE;

    public static final String TAG = LanguageUtil.class.getSimpleName();
    private Context ctx = null;

    private LanguageUtil()
    {
    }

    public void setContext(Context ctx)
    {
        if (this.ctx == null) {
            this.ctx = ctx;
        }
    }

    public String errorTitleFor(final String errorId, final ErrorType errorType)
    {
        return errorTranslation(errorId, errorType, "title", "");
    }

    public String errorMessageFor(final String errorId, final ErrorType errorType)
    {
        return errorTranslation(errorId, errorType, "msg", "");
    }

    /**
     * Gets an error translation from the resource files.
     *
     * @param errorId   The error id.
     * @param errorType The type of the error.
     * @param postfix   A postfix of the error resource id.
     * @param variant   String to differentiate variants of the error message.
     * @return The localizes error translation.
     */
    private String errorTranslation(final String errorId, final ErrorType errorType, String postfix, String variant)
    {
        if (!variant.equals("")) {
            variant = "_" + variant;
        }
        String[] errorMessageCandidates = {
                errorType.getCategory().toString() + variant + "_" + errorId,
                errorType.getCategory().toString() + "_" + errorId,
                errorType.getBaseCode(),
                errorType.getCategory().toString()
        };

        int resId = getErrorMessageResourceId(errorMessageCandidates, postfix);

        if (resId <= 0) {
            return "";
        }
        return this.ctx.getString(resId);
    }

    /**
     * Checks whether an error translation exists or not.
     *
     * @param errorId   The error id.
     * @param errorType The type of the error.
     * @param postfix   A postfix of the error resource id.
     * @param variant   String to differentiate variants of the error message.
     * @return <code>true</code> if the error translation exists, otherwise <code>false</code>.
     */
    public boolean hasErrorTranslation(final String errorId, final ErrorType errorType, String postfix, String variant)
    {
        if (!variant.equals("")) {
            variant = "_" + variant;
        }

        String[] errorMessageCandidates = {
                errorType.getCategory().toString() + variant + "_" + errorId,
                errorType.getCategory().toString() + "_" + errorId,
        };

        int resId = getErrorMessageResourceId(errorMessageCandidates, postfix);
        return resId > 0;
    }

    /**
     * Determines the resource id for an error message from a set of candidates.
     *
     * @param errorMessageCandidates A {@link String} array of error message candidates.
     * @param postfix                The postfix of the error message translation key.
     * @return The resource id number on success, otherwise 0.
     */
    private int getErrorMessageResourceId(String[] errorMessageCandidates, String postfix)
    {
        for (String errorMessageCandidate : errorMessageCandidates) {

            String errorMessage = ErrorMappings.mapErrorMessage(errorMessageCandidate.toLowerCase(Locale.US));
            String translationKey = ("error_" + errorMessage + "_" + postfix).toLowerCase(Locale.US);

            int resId = this.ctx.getResources().getIdentifier(translationKey, "string", this.ctx.getPackageName());
            if (resId > 0) {
                return resId;
            } else {
                Log.v(TAG, "translation key not present: " + translationKey);
            }
        }
        return 0;
    }

    public boolean hasErrorTranslation(SystemError sysErr, String postfix, String variant)
    {
        return hasErrorTranslation(sysErr.getCode(), sysErr.getType(), postfix, variant);
    }

    public String errorTitleFor(SystemError sysErr, String variant)
    {
        return errorTranslation(sysErr.getCode(), sysErr.getType(), "title", variant);
    }

    public String errorMessageFor(SystemError sysErr, String variant)
    {
        return errorTranslation(sysErr.getCode(), sysErr.getType(), "msg", variant);
    }

}
