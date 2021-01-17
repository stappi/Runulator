package com.stappert.runulator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.preference.Preference;

import java.time.Period;
import java.util.Calendar;
import java.util.Date;
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
        return context.getSharedPreferences("runulator", Context.MODE_PRIVATE);
    }

    /**
     * Calculates the age.
     *
     * @param birthday birthday
     * @return age
     */
    public static int calculateAge(long birthday) {
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(birthday);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if ((today.get(Calendar.MONTH) < dob.get(Calendar.MONTH))
                || (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }
        return age;
    }
}
