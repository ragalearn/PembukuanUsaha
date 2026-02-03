package com.example.pembukuanusaha.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class RupiahFormatter {

    public static String format(int angka) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(localeID);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(angka);
    }
}
