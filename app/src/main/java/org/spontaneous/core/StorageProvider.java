package org.spontaneous.core;

import android.content.Context;

import java.util.Map;

/**
 * Abstracts key-value based storage, allowing its implementations
 * varying significantly and easy storage method switching.
 *
 * For specific usages see StorageUtil and implementations like HardenedStorageProvider.
 *
 * @author Dominik Dzienia
 * @see org.spontaneous.utility.StorageUtil
 * @see org.spontaneous.utility.provider.HardenedStorageProvider
 */
public interface StorageProvider {
    void setContext(Context ctx);

    boolean store(String domain, String key, String value);

    boolean store(String domain, String key, boolean value);

    boolean store(String domain, Map<String, String> turples);

    String restore(String domain, String key, String defaultValue);

    boolean restore(String domain, String key, boolean defaultValue);

    Map<String, String> restore(String domain, Map<String, String> turplesWithDefaults);
}
