package org.spontaneous.core.common.error;

import java.util.HashMap;

public class ErrorMappings {
    private final static HashMap<String, String> errorRemaping;

    /**
     * This mapping is used to map backend error codes to internal error codes (translation keys).
     */
    static {
        errorRemaping = new HashMap<>();
        errorRemaping.put("auth_invalid_grant", "auth_wrong_credentials");
        errorRemaping.put("unsupported_grant_type", "fatal");
        errorRemaping.put("auth_unauthorized", "auth_wrong_credentials");
        errorRemaping.put("auth_unauthorized_user", "auth_wrong_credentials");
        errorRemaping.put("auth_credentials_expired", "auth_password_expired");
        errorRemaping.put("client_bad_response", "fatal");
        errorRemaping.put("client_invalid_data", "fatal");
        errorRemaping.put("bad_response", "fatal");
        errorRemaping.put("backend_internal_server_error", "fatal");
        errorRemaping.put("client_invalid_api_version", "client_update_needed");
        errorRemaping.put("client_invalid_app_version", "client_update_needed");
        errorRemaping.put("client_invalid_app_key", "client_update_needed");
        errorRemaping.put("network_sync_network_offline", "network_sync_network_error");
    }

    public static String mapErrorMessage(String errorMessageCandidate)
    {
        if (errorRemaping.containsKey(errorMessageCandidate)) {
            return errorRemaping.get(errorMessageCandidate);
        }

        return errorMessageCandidate;
    }
}
