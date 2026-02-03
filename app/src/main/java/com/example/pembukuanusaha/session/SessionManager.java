package com.example.pembukuanusaha.session;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.pembukuanusaha.activity.LoginActivity;

import java.util.HashMap;

public class SessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // Mode Shared Preferences
    int PRIVATE_MODE = 0;

    // Nama File Sharedpref
    private static final String PREF_NAME = "SesiUsaha";

    // Semua Kunci Shared Preferences
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_UID = "uid";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ROLE = "role";
    public static final String KEY_USAHA_ID = "usaha_id";
    public static final String KEY_CABANG_ID = "cabang_id";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Membuat sesi login (Menyimpan data user ke HP)
     */
    public void createLoginSession(String uid, String email, String role, String usahaId, String cabangId) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_USAHA_ID, usahaId);
        editor.putString(KEY_CABANG_ID, cabangId);
        editor.commit(); // Simpan perubahan
    }

    /**
     * Cek status login user
     * Jika false, akan diarahkan ke LoginActivity
     */
    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    /**
     * Mendapatkan Data User yang tersimpan
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_UID, pref.getString(KEY_UID, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_ROLE, pref.getString(KEY_ROLE, null));
        user.put(KEY_USAHA_ID, pref.getString(KEY_USAHA_ID, null));
        user.put(KEY_CABANG_ID, pref.getString(KEY_CABANG_ID, null));
        return user;
    }

    /**
     * Cek apakah user sedang login
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    /**
     * Cek apakah User adalah ADMIN
     */
    public boolean isAdmin() {
        String role = pref.getString(KEY_ROLE, "user");
        return role != null && role.equalsIgnoreCase("admin");
    }

    /**
     * Logout user (Hapus sesi)
     */
    public void logoutUser() {
        editor.clear();
        editor.commit();

        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    // ==========================================
    // 🔥 GETTER METHODS (YANG TADI ERROR)
    // ==========================================

    public String getUsahaId() {
        return pref.getString(KEY_USAHA_ID, "");
    }

    public String getCabangId() {
        return pref.getString(KEY_CABANG_ID, "");
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, "");
    }
}