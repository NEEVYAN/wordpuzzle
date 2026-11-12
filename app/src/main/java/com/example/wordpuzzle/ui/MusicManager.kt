package com.example.wordpuzzle.ui

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import com.example.wordpuzzle.R


object MusicManager {
    private var app: Context? = null
    private var prefs: SharedPreferences? = null
    private var player: MediaPlayer? = null
    private var foreground = false
    var enabled = true
        private set

    fun init(ctx: Context) {
        if (app != null) return
        app = ctx.applicationContext
        prefs = app!!.getSharedPreferences("word_puzzle", Context.MODE_PRIVATE)
        enabled = prefs!!.getBoolean("music", true)
    }

    fun onForeground() { foreground = true; startIfNeeded() }
    fun onBackground() { foreground = false; player?.takeIf { it.isPlaying }?.pause() }

    fun setEnabled(on: Boolean) {
        enabled = on
        prefs?.edit()?.putBoolean("music", on)?.apply()
        if (on) startIfNeeded() else player?.takeIf { it.isPlaying }?.pause()
    }

    private fun startIfNeeded() {
        if (!enabled || !foreground) return
        val c = app ?: return
        if (player == null) {
            player = MediaPlayer.create(c, R.raw.music)?.apply { isLooping = true; setVolume(.4f, .4f) }
        }
        player?.takeIf { !it.isPlaying }?.start()
    }
}
