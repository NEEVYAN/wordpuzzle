package com.example.wordpuzzle.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import com.example.wordpuzzle.data.Level
import kotlin.math.min

class CrosswordView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) : View(ctx, attrs) {
    private var level: Level? = null
    private var revealed: Set<Pair<Int, Int>> = emptySet()
    private var popping: Set<Pair<Int, Int>> = emptySet()
    private var pop = 1f
    private var popAnim: ValueAnimator? = null
    private val tile = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1.5f.dp; color = 0x59FFFFFF }
    private val rect = RectF()

    fun setState(lv: Level, found: Set<String>) {
        val prev = level
        val now = lv.placed.filter { it.word in found }.flatMap { it.cells() }.toSet()
        val added = now - revealed
        level = lv
        revealed = now
        if (prev != null && prev.index != lv.index) {
            popAnim?.cancel(); popping = emptySet(); pop = 1f
            alpha = 0f; animate().alpha(1f).setDuration(500).start()
        } else if (added.isNotEmpty() && prev != null) {
            popping = added
            popAnim?.cancel()
            popAnim = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 450
                interpolator = OvershootInterpolator(2.5f)
                addUpdateListener { pop = it.animatedValue as Float; invalidate() }
                doOnEnd { popping = emptySet(); invalidate() }
                start()
            }
        }
        invalidate()
    }

    override fun onDetachedFromWindow() { popAnim?.cancel(); super.onDetachedFromWindow() }

    override fun onDraw(c: Canvas) {
        val lv = level ?: return
        val cell = min(width.toFloat() / lv.cols, height.toFloat() / lv.rows).coerceAtMost(56.dp)
        val ox = (width - cell * lv.cols) / 2
        val oy = (height - cell * lv.rows) / 2
        val gap = cell * 0.06f
        for ((pos, ch) in lv.cells) {
            val cx = ox + pos.second * cell + cell / 2
            val cy = oy + pos.first * cell + cell / 2
            val on = pos in revealed
            val s = if (pos in popping) pop.coerceAtLeast(0f) else 1f
            val half = (cell / 2 - gap) * s
            val rr = cell * .2f
            if (on) {
                tile.color = Palette.TILE_ON_DARK
                rect.set(cx - half, cy - half + cell * .07f, cx + half, cy + half + cell * .07f)
                c.drawRoundRect(rect, rr, rr, tile)
                tile.color = Palette.TILE_ON
                rect.set(cx - half, cy - half, cx + half, cy + half)
                c.drawRoundRect(rect, rr, rr, tile)
            } else {
                tile.color = Palette.TILE_OFF
                rect.set(cx - half, cy - half, cx + half, cy + half)
                c.drawRoundRect(rect, rr, rr, tile)
                c.drawRoundRect(rect, rr, rr, stroke)
            }
            if (on) {
                text.textSize = cell * .5f * s
                c.drawText(ch.toString(), cx, cy - (text.ascent() + text.descent()) / 2, text)
            }
        }
    }
}
