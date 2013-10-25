package kr.pe.kingori.peoplematcher.util;

import android.content.Context;
import android.content.SharedPreferences;
import kr.pe.kingori.peoplematcher.Application;

import java.util.HashMap;

public class SharedPreferenceUtil {
    private static HashMap<String, Object> memoryCache = new HashMap<String, Object>();
    private static final String SHARED_PREFERENCE_NAME = "game_app";

    private SharedPreferenceUtil() {
    }

    public static void put(String key, int value) {
        memoryCache.put(key, value);
        getSharedPreference().edit().putInt(key, value).commit();
    }

    public static void put(String key, String value) {
        memoryCache.put(key, value);
        getSharedPreference().edit().putString(key, value).commit();
    }

    public static void put(String key, long value) {
        memoryCache.put(key, value);
        getSharedPreference().edit().putLong(key, value).commit();
    }

    public static int getInt(String key) {
        Integer result = getValueFromCache(key);
        if (result == null) {
            result = getSharedPreference().getInt(key, 0);
            memoryCache.put(key, result);
        }
        return result;
    }

    public static long getLong(String key) {
        Long result = getValueFromCache(key);
        if (result == null) {
            result = getSharedPreference().getLong(key, 0L);
            memoryCache.put(key, result);
        }
        return result;
    }

    public static String getString(String key) {
        String result = getValueFromCache(key);
        if (result == null) {
            result = getSharedPreference().getString(key, null);
            memoryCache.put(key, result);
        }
        return result;
    }

    public static <T> T getValueFromCache(String key) {
        if (memoryCache.containsKey(key)) {
            return (T) memoryCache.get(key);
        } else {
            return null;
        }
    }

    private static SharedPreferences getSharedPreference() {
        return Application.context
                .getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

}
