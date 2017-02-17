package org.spontaneous.utility;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maweiss on 28.04.2016.
 */
public class StringUtils {

    /**
     * Returns whether a string is empty. Null is considered as empty string.
     *
     * @param string string to check
     */
    public static boolean isEmptyString(String string)
    {
        return string == null || string.isEmpty();
    }

    /**
     * Returns the most frequent string in an array.
     *
     * @param strings Array of strings
     * @return Most frequent string
     */
    public static String getMostFrequentString(String... strings)
    {
        // Count strings
        Map<String, Integer> countMap = new HashMap<String, Integer>();
        for (String string : strings) {
            Integer currentCount = countMap.get(string);
            if (null == currentCount) {
                countMap.put(string, 1);
            } else {
                countMap.put(string, currentCount + 1);
            }
        }

        // Retrieve most frequent string
        Integer maxCount = null;
        String mostFrequentString = null;
        for (String string : countMap.keySet()) {
            if (maxCount == null) {
                maxCount = countMap.get(string);
                mostFrequentString = string;
            } else {
                Integer currentCount = countMap.get(string);
                if (currentCount > maxCount) {
                    maxCount = currentCount;
                    mostFrequentString = string;
                }
            }
        }

        return mostFrequentString;
    }

    /**
     * Converts parcel code to uppercase and deletes all spaces.
     *
     * @param parcelCode Parcel code
     * @return normalized parcel code
     */
    public static String normalizeParcelCode(String parcelCode)
    {
        String result = parcelCode.toUpperCase();
        return result.replaceAll(" ", "");
    }
}
