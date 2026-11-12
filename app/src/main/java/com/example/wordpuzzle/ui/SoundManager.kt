package com.example.wordpuzzle.ui

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.wordpuzzle.R

object SoundManager {
    private var pool: SoundPool? = null
    private val ids = HashMap<Int, Int>()
    private var prefs: SharedPreferences? = null
    var enabled = true
        private set

    fun init(ctx: Context) {
        if (pool != null) return
        val app = ctx.applicationContext
        prefs = app.getSharedPreferences("word_puzzle", Context.MODE_PRIVATE)
        enabled = prefs!!.getBoolean("sound", true)
        val p = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        ).build()
        listOf(R.raw.tap, R.raw.click, R.raw.valid, R.raw.bonus, R.raw.error, R.raw.win).forEach { ids[it] = p.load(app, it, 1) }
        pool = p
    }

    fun toggle(): Boolean {
        enabled = !enabled
        prefs?.edit()?.putBoolean("sound", enabled)?.apply()
        play(R.raw.click)
        return enabled
    }

    fun setEnabled(on: Boolean) {
        enabled = on
        prefs?.edit()?.putBoolean("sound", on)?.apply()
    }

    fun play(res: Int, rate: Float = 1f) {
        if (!enabled) return
        val id = ids[res] ?: return
        pool?.play(id, 1f, 1f, 1, 0, rate.coerceIn(.5f, 2f))
    }
}
