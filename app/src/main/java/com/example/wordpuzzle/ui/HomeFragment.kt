package com.example.wordpuzzle.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wordpuzzle.R
import com.example.wordpuzzle.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: GameViewModel by activityViewModels()
    private val loops = mutableListOf<ObjectAnimator>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        b.btnSettings.setOnClickListener { SoundManager.play(R.raw.click); parentFragmentManager.push(SettingsFragment(), SettingsFragment.TAG) }
        b.btnPlay.setOnClickListener { SoundManager.play(R.raw.click); parentFragmentManager.push(LevelSelectFragment(), LevelSelectFragment.TAG) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.coins.collect { b.homeCoins.text = getString(R.string.coins_n, it) }
            }
        }

        b.logo.scaleX = 0.6f
        b.logo.scaleY = 0.6f
        b.logo.alpha = 0f
        b.logo.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator())
            .start()


        loops += ObjectAnimator.ofFloat(b.logo, View.TRANSLATION_Y, 0f, -14.dp, 0f).apply {
            duration = 2600
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        loops += ObjectAnimator.ofFloat(b.logo, View.ROTATION, -2f, 2f).apply {
            duration = 3200
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        loops += ObjectAnimator.ofPropertyValuesHolder(
            b.btnPlay, PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.06f), PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.06f)
        ).apply { duration = 900; repeatCount = ObjectAnimator.INFINITE; repeatMode = ObjectAnimator.REVERSE; start() }
    }

    override fun onDestroyView() {
        loops.forEach { it.cancel() }; loops.clear()
        _b = null
        super.onDestroyView()
    }
}
