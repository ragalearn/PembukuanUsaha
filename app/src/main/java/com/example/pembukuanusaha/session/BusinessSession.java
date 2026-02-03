package com.example.pembukuanusaha.session;

import android.content.Context;
import android.content.SharedPreferences;

public class BusinessSession {

    private static final String PREF = "business_session";
    private static final String KEY_USAHA = "usaha_id";
    private static final String KEY_CABANG = "cabang_id";

    SharedPreferences sp;

    public BusinessSession(Context context) {
        sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void setUsaha(String usahaId) {
        sp.edit().putString(KEY_USAHA, usahaId).apply();
    }

    public void setCabang(String cabangId) {
        sp.edit().putString(KEY_CABANG, cabangId).apply();
    }

    public String getUsaha() {
        return sp.getString(KEY_USAHA, null);
    }

    public String getCabang() {
        return sp.getString(KEY_CABANG, null);
    }

    public boolean isReady() {
        return getUsaha() != null && getCabang() != null;
    }
}
