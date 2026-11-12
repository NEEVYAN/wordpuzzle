package com.example.wordpuzzle.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wordpuzzle.R
import com.example.wordpuzzle.databinding.FragmentLevelSelectBinding
import com.example.wordpuzzle.databinding.ItemLevelBinding
import kotlinx.coroutines.launch

class LevelSelectFragment : Fragment() {
    companion object { const val TAG = "levels" }

    private var _b: FragmentLevelSelectBinding? = null
    private val b get() = _b!!
    private val vm: GameViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentLevelSelectBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        val adapter = Adapter(vm.levelCount) { idx ->
            vm.openLevel(idx)
            parentFragmentManager.push(GameFragment(), GameFragment.TAG)
        }
        b.levels.layoutManager = GridLayoutManager(requireContext(), 4)
        b.levels.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.unlocked.collect { adapter.unlocked = it }
            }
        }
    }

    override fun onDestroyView() {
        b.levels.adapter = null
        _b = null
        super.onDestroyView()
    }

    private class Adapter(val count: Int, val onClick: (Int) -> Unit) : RecyclerView.Adapter<Adapter.VH>() {
        var unlocked = 0
            set(v) { field = v; notifyDataSetChanged() }

        class VH(val b: ItemLevelBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemLevelBinding.inflate(LayoutInflater.from(p.context), p, false))
        override fun getItemCount() = count

        override fun onBindViewHolder(h: VH, pos: Int) {
            val open = pos <= unlocked
            h.b.num.text = if (open) "${pos + 1}" else "🔒"
            h.b.num.setBackgroundResource(when { pos < unlocked -> R.drawable.bg_level_done; pos == unlocked -> R.drawable.bg_button; else -> R.drawable.bg_level_locked })
            h.b.num.setOnClickListener(if (open) View.OnClickListener { SoundManager.play(R.raw.click); onClick(pos) } else null)
            h.b.num.isClickable = open
            h.itemView.alpha = 0f
            h.itemView.scaleX = .8f; h.itemView.scaleY = .8f
            h.itemView.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(pos * 30L).setDuration(250).start()
        }
    }
}
