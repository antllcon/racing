package com.mobility.race.data
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

val MapStringType = object : NavType<Map<String, String>>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Map<String, String>? {
        val jsonString = bundle.getString(key)
        return jsonString?.let {
            Json.decodeFromString<Map<String, String>>(it)
        }
    }

    override fun put(bundle: Bundle, key: String, value: Map<String, String>) {
        val jsonString = Json.encodeToString(value)
        bundle.putString(key, jsonString)
    }

    override fun parseValue(value: String): Map<String, String> {
        return Json.decodeFromString(value)
    }
}