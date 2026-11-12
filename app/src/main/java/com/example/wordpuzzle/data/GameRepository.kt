package com.example.wordpuzzle.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject


class GameRepository(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("word_puzzle", Context.MODE_PRIVATE)

    val levels: List<Level> by lazy {
        val txt = app.assets.open("levels.json").bufferedReader().use { it.readText() }
        val arr = JSONObject(txt).getJSONArray("levels")
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CrosswordGenerator.build(
                i, LevelData(o.getString("letters"), o.getJSONArray("words").strings(), o.optJSONArray("bonus")?.strings() ?: emptyList())
            )
        }
    }

    private fun JSONArray.strings() = (0 until length()).map { getString(it) }

    var unlocked: Int
        get() = prefs.getInt("unlocked", 0)
        set(v) = prefs.edit().putInt("unlocked", v).apply()
    var coins: Int
        get() = prefs.getInt("coins", 0)
        set(v) = prefs.edit().putInt("coins", v).apply()
    var current: Int
        get() = prefs.getInt("current", 0)
        set(v) = prefs.edit().putInt("current", v).apply()

    fun found(level: Int): Set<String> = prefs.getStringSet("found_$level", emptySet())!!.toSet()
    fun bonus(level: Int): Set<String> = prefs.getStringSet("bonus_$level", emptySet())!!.toSet()
    fun save(level: Int, found: Set<String>, bonus: Set<String>) =
        prefs.edit().putStringSet("found_$level", found).putStringSet("bonus_$level", bonus).apply()
    fun clear(level: Int) = prefs.edit().remove("found_$level").remove("bonus_$level").apply()
}
