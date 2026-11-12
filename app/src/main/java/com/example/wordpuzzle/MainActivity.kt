package com.example.wordpuzzle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.wordpuzzle.ui.HomeFragment
import com.example.wordpuzzle.ui.MusicManager
import com.example.wordpuzzle.ui.SoundManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundManager.init(this)
        MusicManager.init(this)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.commit { replace(R.id.container, HomeFragment(), "home") }
        }
    }

    override fun onStart() { super.onStart(); MusicManager.onForeground() }
    override fun onStop() { MusicManager.onBackground(); super.onStop() }
}
