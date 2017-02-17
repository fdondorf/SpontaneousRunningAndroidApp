package org.spontaneous.core.common.error;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import org.spontaneous.utility.LanguageUtil;
import org.spontaneous.utility.JSONHelper;


public class SystemError {
    public static final String TAG = SystemError.class.getSimpleName();

    public static final SystemError UNKNOWN = new SystemError(ErrorType.UNKNOWN);
    public static final SystemError NO_ERROR = new SystemError(ErrorType.NO_ERROR);
    public static final SystemError USER_CANCELED = new SystemError(ErrorType.USER_CANCELED);
    public static final SystemError TIMEOUT = new SystemError(ErrorType.TIMEOUT);
    public static final SystemError FATAL = new SystemError(ErrorType.INTERNAL_SERVER_ERROR);


    private final ErrorType type;
    private String code;
    private String message;
    private String displayMessage;
    private String logId;

    public SystemError(ErrorType type)
    {
        super();
        this.type = type;
        setCode(this.type.getBaseCode());
    }

    private SystemError(String jsonPayload)
    {
        super();
        ErrorType typeToSet = ErrorType.UNKNOWN;
        try {
            JSONObject config = new JSONObject(jsonPayload);
            typeToSet = ErrorType.parseInt(config.getInt("type"));
            this.code = JSONHelper.getStringFailsafe(config, "code", typeToSet.getBaseCode());
        } catch (JSONException e) {
            Log.e(TAG, "SystemError deserialization error", e);
        } finally {
            this.type = typeToSet;
        }
    }

    public SystemError(ErrorType type, String message)
    {
        this.type = type;
        this.code = this.type.getBaseCode();
        this.message = message;
    }

    public ErrorType getType()
    {
        return type;
    }

    public String getCode()
    {
        return code;
    }

    public String toJSON()
    {
        JSONObject to = new JSONObject();
        try {
            to.put("type", type.getCode());
            to.put("code", code);
        } catch (JSONException e) {
            Log.e(TAG, "SystemError serialization error", e);
        }
        return to.toString();
    }

    public static SystemError fromJSON(String errorPalyoad)
    {
        return new SystemError(errorPalyoad);
    }

    public static SystemError fromWebServicePayload(int errorStatus, JSONObject errorPalyoad)
    {

        ErrorType typeToSet = ErrorType.UNKNOWN;


        typeToSet = ErrorType.parseInt(errorStatus);
        SystemError err = new SystemError(typeToSet);

        err.setCode(JSONHelper.getStringFailsafe(errorPalyoad, "error", typeToSet.getBaseCode()));
        err.setMessage(JSONHelper.getStringFailsafe(errorPalyoad, "error_description", typeToSet.getBaseMessage()));
        err.setLogId(JSONHelper.getStringFailsafe(errorPalyoad, "error_log_id", null));

        return err;


    }

    private void setCode(String newCode)
    {
        code = newCode;
        this.setDisplayMessage(LanguageUtil.INSTANCE.errorMessageFor(code, this.getType()));
    }

    public boolean ofType(ErrorType errType)
    {
        return this.type.equals(errType);
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getLogId()
    {
        return logId;
    }

    public void setLogId(String logId)
    {
        this.logId = logId;
    }

    public String getDisplayMessage()
    {
        if (displayMessage == null) {
            return "";
        }
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage)
    {
        this.displayMessage = displayMessage;
    }

    public SystemError thatCanBeShown()
    {
        if (!LanguageUtil.INSTANCE.hasErrorTranslation(this.getCode(), this.getType(), "msg", "login")) {
            return SystemError.FATAL;
        }
        return this;
    }
}
