package com.stappert.runulator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.preference.Preference;

import java.util.Set;

public class Utils {

    /**
     * Returns desired string via id name.
     *
     * @param context context
     * @param idName  id name
     * @return string
     */
    public static String getStringByIdName(Context context, String idName) {
        Resources resources = context.getResources();
        return resources.getString(resources.getIdentifier(idName, "string", context.getPackageName()));
    }



    /**
     * Returns shared preferences.
     *
     * @param context context of application
     * @return shared preferences
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences( "runulator", Context.MODE_PRIVATE);
    }
}
