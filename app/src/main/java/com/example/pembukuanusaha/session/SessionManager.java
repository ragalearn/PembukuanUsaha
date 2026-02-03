package com.example.pembukuanusaha.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "user_session";

    private static final String KEY_ROLE = "role";
    private static final String KEY_USAHA_ID = "usaha_id";
    private static final String KEY_CABANG_ID = "cabang_id";

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_KASIR = "kasir";

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /* =====================
       ROLE
       ===================== */

    public void setRole(String role) {
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, ROLE_ADMIN);
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(getRole());
    }

    public boolean isKasir() {
        return ROLE_KASIR.equals(getRole());
    }

    /* =====================
       USAHA & CABANG
       ===================== */

    public void setUsaha(String usahaId, String cabangId) {
        editor.putString(KEY_USAHA_ID, usahaId);
        editor.putString(KEY_CABANG_ID, cabangId);
        editor.apply();
    }

    public String getUsahaId() {
        return pref.getString(KEY_USAHA_ID, "");
    }

    public String getCabangId() {
        return pref.getString(KEY_CABANG_ID, "");
    }
}
