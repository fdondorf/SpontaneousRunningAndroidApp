package org.spontaneous.core.api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An App-wide interface for JSON Serialization / Deserialization.
 * Used when passing App DAO / business objects by Intents, Broadcasts and
 * Activity save states.
 *
 * It should be used instead of standard Java (Serializable) or Android specific
 * (Parcelable) interfaces, because it is used not only for in-app
 * serializations, but also to parse and build web service related traffic.
 *
 * @param <T> class type
 * @author Dominik Dzienia
 */
public interface SerializableJSON<T> {
    /**
     * Stores the current object state into a {@link JSONObject}.
     *
     * @return The {@link JSONObject} representing the object state.
     * @throws JSONException Thrown in case an error concerning the JSON API appears.
     */
    JSONObject storeInJSON() throws JSONException;

    /**
     * Restores an instance of the specific class from the information
     * provided in the {@link JSONObject}.
     *
     * @param to The data source of the new instance.
     * @return The newly created class object.
     * @throws JSONException Thrown in case an error concerning the JSON API appears.
     */
    T restoreFromJSON(JSONObject to) throws JSONException;
}
