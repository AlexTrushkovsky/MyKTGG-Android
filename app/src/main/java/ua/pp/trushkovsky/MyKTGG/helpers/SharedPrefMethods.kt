package ua.pp.trushkovsky.MyKTGG.helpers

import android.content.Context
import android.content.SharedPreferences

fun saveStringToSharedPreferences(key: String, value: String, context: Context?) {
    val settings = context?.getSharedPreferences("default", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = settings?.edit() ?: return
    editor.putString(key, value)
    editor.apply()
}
fun saveBoolToSharedPreferences(key: String, value: Boolean, context: Context?) {
    val settings = context?.getSharedPreferences("default", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = settings?.edit() ?: return
    editor.putBoolean(key, value)
    editor.apply()
}
fun saveIntToSharedPreferences(key: String, value: Int, context: Context?) {
    val settings = context?.getSharedPreferences("default", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = settings?.edit() ?: return
    editor.putInt(key, value)
    editor.apply()
}

fun getIntFromSharedPreferences(key: String, context: Context?): Int {
    val settings = context?.getSharedPreferences("default", Context.MODE_PRIVATE)
    if (settings != null) {
        return settings.getInt(key, -1)
    }
    return -1
}
fun getStringFromSharedPreferences(key: String, context: Context?): String {
    val settings = context?.getSharedPreferences("default", Context.MODE_PRIVATE)
    if (settings != null) return settings.getString(key, "") ?: return ""
    return ""
}
fun getBoolFromSharedPreferences(key: String, context: Context?): Boolean {
    val settings = context?.getSharedPreferences("default", Context.MODE_PRIVATE)
    if (settings != null) {
        return settings.getBoolean(key, true)
    }
    return true
}

//Global prefs don't getting erased on log out
fun saveStringToGlobalPreferences(key: String, value: String, context: Context?) {
    val settings = context?.getSharedPreferences("global", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = settings?.edit() ?: return
    editor.putString(key, value)
    editor.apply()
}
fun saveBoolToGlobalPreferences(key: String, value: Boolean, context: Context?) {
    val settings = context?.getSharedPreferences("global", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = settings?.edit() ?: return
    editor.putBoolean(key, value)
    editor.apply()
}
fun saveIntToGlobalPreferences(key: String, value: Int, context: Context?) {
    val settings = context?.getSharedPreferences("global", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = settings?.edit() ?: return
    editor.putInt(key, value)
    editor.apply()
}

fun getIntFromGlobalPreferences(key: String, context: Context?): Int {
    val settings = context?.getSharedPreferences("global", Context.MODE_PRIVATE)
    if (settings != null) {
        return settings.getInt(key, -1)
    }
    return -1
}
fun getStringFromGlobalPreferences(key: String, context: Context?): String {
    val settings = context?.getSharedPreferences("global", Context.MODE_PRIVATE)
    if (settings != null) return settings.getString(key, "") ?: return ""
    return ""
}
fun getBoolFromGlobalPreferences(key: String, context: Context?): Boolean {
    val settings = context?.getSharedPreferences("global", Context.MODE_PRIVATE)
    if (settings != null) {
        return settings.getBoolean(key, true)
    }
    return true
}