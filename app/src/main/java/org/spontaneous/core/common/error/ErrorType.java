package org.spontaneous.core.common.error;

import org.spontaneous.utility.LanguageUtil;

import java.util.HashMap;
import java.util.Locale;


public enum ErrorType {

    NO_ERROR(0, ErrorCategory.OTHER),
    EMPTY_RESPONSE(901, ErrorCategory.BACKEND),
    UNDEFINED_STATUS(902, ErrorCategory.BACKEND),
    USER_CANCELED(801, ErrorCategory.USER),
    INVALID_RESPONSE_JSON(301, ErrorCategory.BACKEND),
    UNAUTHORIZED(401, ErrorCategory.AUTH),
    BAD_RESPONSE(400, ErrorCategory.CLIENT),
    INTERNAL_SERVER_ERROR(500, ErrorCategory.BACKEND),
    CLIENT_PROTOCOL_ERROR(610, ErrorCategory.NETWORK),
    NETWORK_ERROR(600, ErrorCategory.NETWORK),
    NETWORK_OFFLINE(601, ErrorCategory.NETWORK),
    CLIENT_TIMEOUT(680, ErrorCategory.NETWORK),
    TIMEOUT(690, ErrorCategory.NETWORK),
    DEVICE_HARDWARE_ERROR(710, ErrorCategory.CLIENT),
    UNKNOWN(-1, ErrorCategory.OTHER);

    private final int code;
    private final String baseCode;
    private final ErrorCategory category;

    private static HashMap<Integer, ErrorType> parseCache = new HashMap<>();

    private ErrorType(int errorCode, ErrorCategory errorCategory)
    {
        this.code = errorCode;
        this.baseCode = this.toString().toLowerCase(Locale.US);
        this.category = errorCategory;
    }

    public int getCode()
    {
        return code;
    }

    public ErrorCategory getCategory()
    {
        return category;
    }

    public static ErrorType parseInt(int mapping)
    {
        if (parseCache.containsKey(mapping)) {
            return parseCache.get(mapping);
        } else {
            ErrorType[] allPosible = ErrorType.values();
            for (ErrorType checked : allPosible) {
                if (checked.getCode() == mapping) {
                    parseCache.put(mapping, checked);
                    return checked;
                }
            }
            return UNKNOWN;
        }
    }

    public String getBaseCode()
    {
        return baseCode;
    }

    public String getBaseMessage()
    {
        return LanguageUtil.INSTANCE.errorMessageFor(baseCode, this);
    }
}