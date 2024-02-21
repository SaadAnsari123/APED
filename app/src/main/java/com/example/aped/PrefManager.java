package com.example.aped;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefManager {

    Context context;

    PrefManager(Context context) {
        this.context = context;
    }

    public void saveHist(String str) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("History", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(str,"");
        editor.commit();
    }

    public String getstr() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("History", Context.MODE_PRIVATE);
        return sharedPreferences.getString("str","");
    }

}

