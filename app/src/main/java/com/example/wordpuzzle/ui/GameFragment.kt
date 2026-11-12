package com.example.wordpuzzle.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wordpuzzle.R
import com.example.wordpuzzle.databinding.FragmentGameBinding
import kotlinx.coroutines.launch

class GameFragment : Fragment() {
    companion object { const val TAG = "game" }

    private var _b: FragmentGameBinding? = null
    private val b get() = _b!!
    private val vm: GameViewModel by activityViewModels()
    private var leaving = false
    private var lastBonus = -1
    private var shownCoins = -1
    private var coinAnim: android.animation.ValueAnimator? = null

    private fun click() = SoundManager.play(R.raw.click)

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentGameBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, saved: Bundle?) {
        val o = b.overlays
        b.btnPause.setOnClickListener { click(); vm.pause() }
        b.btnShuffle.setOnClickListener { click(); vm.shuffle() }
        o.btnResume.setOnClickListener { click(); vm.resume() }
        o.btnSettings.setOnClickListener { click(); parentFragmentManager.push(SettingsFragment(), SettingsFragment.TAG) }
        o.btnRestart.setOnClickListener { click(); vm.restart() }
        o.btnLevels.setOnClickListener { click(); leave(false) }
        o.btnCompleteLevels.setOnClickListener { click(); leave(false) }
        o.btnHome.setOnClickListener { click(); leave(true) }

        // Back: play -> pause -> resume; on the final completed level it leaves the screen.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val s = vm.state.value
                when {
                    s.completed -> if (s.isLast) leave(false)
                    s.paused -> vm.resume()
                    else -> vm.pause()
                }
            }
        })

        b.wheel.onPreview = { t ->
            if (t.isNotEmpty()) {
                b.preview.animate().cancel()
                b.preview.translationX = 0f
                b.preview.text = t
                b.preview.background = pill(Palette.PREVIEW)
                b.preview.alpha = 1f
            }
        }
        b.wheel.onSubmit = { w -> feedback(w, vm.submit(w)) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.state.collect(::render) }
                launch { vm.coins.collect(::showCoins) }
            }
        }
    }

    private fun showCoins(v: Int) {
        coinAnim?.cancel()
        if (shownCoins < 0 || v < shownCoins) {
            shownCoins = v
            b.coinCount.text = getString(R.string.coins_n, v)
            return
        }
        val from = shownCoins
        shownCoins = v
        coinAnim = android.animation.ValueAnimator.ofInt(from, v).apply {
            duration = 900
            addUpdateListener { _b?.coinCount?.text = getString(R.string.coins_n, it.animatedValue as Int) }
            start()
        }
    }

    private fun leave(toHome: Boolean) {
        val fm = parentFragmentManager
        if (leaving || fm.isStateSaved) return
        leaving = true
        if (toHome) fm.popBackStack(LevelSelectFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE) else fm.popBackStack()
    }

    private fun render(s: GameUiState) {
        val o = b.overlays
        b.levelTitle.text = getString(R.string.level_n, s.level.index + 1)
        b.bonusCount.text = "★ ${s.bonusFound.size}"
        if (lastBonus >= 0 && s.bonusFound.size > lastBonus) {
            b.bonusCount.animate().scaleX(1.5f).scaleY(1.5f).setDuration(120).withEndAction {
                _b?.bonusCount?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(150)?.start()
            }.start()
        }
        lastBonus = s.bonusFound.size
        b.grid.setState(s.level, s.found)
        b.wheel.setLetters(s.letters)
        val playable = !s.paused && !s.completed
        b.wheel.isEnabled = playable
        b.btnShuffle.isEnabled = playable
        o.completeTitle.text = getString(R.string.level_complete, s.level.index + 1)
        o.completeReward.text = getString(R.string.reward, GameViewModel.COINS_PER_LEVEL)
        o.completeSub.text = if (s.isLast) getString(R.string.all_done) else getString(R.string.next_level_starting)
        o.btnCompleteLevels.isVisible = s.isLast
        toggle(o.pauseOverlay, o.pauseCard, s.paused && !s.completed)
        toggle(o.completeOverlay, o.completeCard, s.completed)
    }

    private fun toggle(overlay: View, card: View, show: Boolean) {
        if (show == overlay.isVisible) return
        overlay.animate().cancel()
        if (show) {
            overlay.alpha = 0f; overlay.isVisible = true
            card.scaleX = .8f; card.scaleY = .8f
            card.animate().scaleX(1f).scaleY(1f).setInterpolator(OvershootInterpolator()).setDuration(320).start()
            overlay.animate().alpha(1f).setDuration(200).start()
        } else {
            overlay.animate().alpha(0f).setDuration(150).withEndAction { overlay.isVisible = false }.start()
        }
    }

    private fun feedback(w: String, r: WordResult) {
        val (color, label) = when (r) {
            WordResult.VALID -> Palette.OK to w
            WordResult.BONUS -> Palette.BONUS to getString(R.string.bonus_word, w)
            WordResult.ALREADY -> Palette.WARN to getString(R.string.already_found, w)
            WordResult.INVALID -> Palette.BAD to w
        }
        SoundManager.play(when (r) {
            WordResult.VALID -> R.raw.valid
            WordResult.BONUS -> R.raw.bonus
            WordResult.ALREADY -> R.raw.click
            WordResult.INVALID -> R.raw.error
        })
        if (r == WordResult.VALID && vm.state.value.completed) {
            b.root.postDelayed({ SoundManager.play(R.raw.win) }, 350)
            b.confetti.burst()
        }
        b.preview.animate().cancel()
        b.preview.text = label
        b.preview.background = pill(color)
        b.preview.alpha = 1f
        when (r) {
            WordResult.INVALID -> { b.preview.shake(); b.wheel.shake() }
            WordResult.ALREADY -> b.preview.shake()
            else -> ObjectAnimator.ofPropertyValuesHolder(
                b.preview, PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.25f, 1f), PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.25f, 1f)
            ).setDuration(320).start()
        }
        b.preview.animate().alpha(0f).setStartDelay(750).setDuration(250).start()
    }

    private fun pill(color: Int) = GradientDrawable().apply { cornerRadius = 100.dp; setColor(color) }

    override fun onStop() {
        super.onStop()
        if (!requireActivity().isChangingConfigurations) vm.pause()
    }

    override fun onDestroyView() {
        coinAnim?.cancel()
        _b = null
        super.onDestroyView()
    }
}
