package com.example.wordpuzzle.ui

import android.animation.ObjectAnimator
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.example.wordpuzzle.R

val Int.dp: Float get() = this * Resources.getSystem().displayMetrics.density
val Float.dp: Float get() = this * Resources.getSystem().displayMetrics.density

object Palette {
    val ACCENT = 0xFFFF8A3D.toInt()
    val WHEEL_BG = 0xE6FFFFFF.toInt()
    val LETTER = 0xFF3A3470.toInt()
    val TILE_ON = 0xFF3DDC84.toInt()
    val TILE_ON_DARK = 0xFF1E9E5A.toInt()
    val TILE_OFF = 0x40FFFFFF
    val PREVIEW = 0xFF6C63FF.toInt()
    val OK = 0xFF2ECC71.toInt()
    val BONUS = 0xFFFFC107.toInt()
    val WARN = 0xFFFF9800.toInt()
    val BAD = 0xFFE74C3C.toInt()
    val WHITE = Color.WHITE
}

fun View.shake(amplitude: Float = 14f) {
    val a = amplitude.dp
    ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0f, -a, a, -a * .75f, a * .75f, -a * .4f, a * .4f, 0f)
        .setDuration(350).start()
}

fun FragmentManager.push(f: Fragment, tag: String) {
    if (isStateSaved || findFragmentByTag(tag) != null) return
    commit {
        setReorderingAllowed(true)
        setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
        replace(R.id.container, f, tag)
        addToBackStack(tag)
    }
    executePendingTransactions()
}
