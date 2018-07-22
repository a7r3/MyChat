package vipul.in.mychat.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {

    private SharedPreferences sharedPreferences;
    private Context context;

    public SharedPreferenceManager(String type, Context context) {

        this.context = context;
        sharedPreferences = context.getSharedPreferences(type, Context.MODE_PRIVATE);

    }


    public String getData(String uid) {
        return sharedPreferences.getString(uid, "null");
    }

}
