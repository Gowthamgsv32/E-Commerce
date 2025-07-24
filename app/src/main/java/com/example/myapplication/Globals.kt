package com.example.myapplication

import android.content.Context
import android.util.Log
import com.example.myapplication.model.ProductSpec
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit


const val LOG = "PRODUCTSOAP"
fun log(message: Any) {
    Log.d(LOG, message.toString())
}

fun logE(e: Throwable?, message: String) {
    Log.e(LOG, message, e)
}

fun getProductSpecList(context: Context): List<ProductSpec> {
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val json = sharedPref.getString("product_spec_list", null)

    return if (json != null) {
        val type = object : TypeToken<List<ProductSpec>>() {}.type
        Gson().fromJson(json, type)
    } else {
        emptyList()
    }
}

fun saveProductSpecList(context: Context, product: ProductSpec) {
    val product = getProductSpecList(context) + listOf(product)
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sharedPref.edit() {
        val gson = Gson()
        val json = gson.toJson(product)
        putString("product_spec_list", json)
    }
}

fun removeProductSpecList(context: Context, product: ProductSpec) {
    val product = getProductSpecList(context).filter { it.lookupId != product.lookupId }
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sharedPref.edit() {
        val gson = Gson()
        val json = gson.toJson(product)
        putString("product_spec_list", json)
    }
}

fun isLoggedIn(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("is_logged_in", false)
}

fun saveLoggedIn(context: Context){
    val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
}