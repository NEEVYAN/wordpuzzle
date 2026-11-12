package com.example.wordpuzzle.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import kotlin.math.*


class LetterWheelView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) : View(ctx, attrs) {
    var onPreview: ((String) -> Unit)? = null
    var onSubmit: ((String) -> Unit)? = null

    private var letters: List<Char> = emptyList()
    private val selected = ArrayList<Int>()
    private var centers: List<PointF> = emptyList()
    private var scales = FloatArray(0)
    private var appear = 1f
    private var appearAnim: ValueAnimator? = null
    private var fx = 0f
    private var fy = 0f
    private var dragging = false
    private var lastX = Float.NaN
    private var lastY = Float.NaN
    private var radius = 0f
    private var letterR = 0f
    private val path = Path()
    private val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33000000 }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2.dp; color = 0x1F3A3470 }

    private val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.WHEEL_BG }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.ACCENT }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        color = Palette.ACCENT; alpha = 210
    }
    private val txt = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }

    fun setLetters(l: List<Char>) {
        if (l == letters) return
        val first = letters.isEmpty()
        letters = l
        selected.clear()
        scales = FloatArray(l.size) { 1f }
        computeCenters()
        if (!first) {
            appearAnim?.cancel()
            appearAnim = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 380; interpolator = OvershootInterpolator()
                addUpdateListener { appear = it.animatedValue as Float; invalidate() }
                start()
            }
        }
        invalidate()
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        var side = min(MeasureSpec.getSize(wSpec), 320.dp.toInt())
        if (MeasureSpec.getMode(hSpec) != MeasureSpec.UNSPECIFIED) side = min(side, MeasureSpec.getSize(hSpec))
        setMeasuredDimension(side, side)
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        radius = min(w, h) / 2f - 2.dp
        letterR = radius * .21f
        line.strokeWidth = letterR * .45f
        computeCenters()
    }

    private fun computeCenters() {
        if (radius <= 0f || letters.isEmpty()) { centers = emptyList(); return }
        val ring = radius * .64f
        val n = letters.size
        centers = List(n) { i ->
            val a = -PI / 2 + 2 * PI * i / n
            PointF(width / 2f + (ring * cos(a)).toFloat(), height / 2f + (ring * sin(a)).toFloat())
        }
    }

    // ---------- touch ----------
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!isEnabled || centers.size != letters.size || letters.isEmpty()) return false
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                selected.clear(); dragging = true
                lastX = Float.NaN; lastY = Float.NaN
                fx = e.x; fy = e.y
                trackTo(e.x, e.y)
                changed()
            }
            MotionEvent.ACTION_MOVE -> {
                for (h in 0 until e.historySize) trackTo(e.getHistoricalX(h), e.getHistoricalY(h))
                trackTo(e.x, e.y)
                fx = e.x; fy = e.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> finish(true)
            MotionEvent.ACTION_CANCEL -> finish(false)
        }
        return true
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled && dragging) finish(false)
    }

    private fun finish(submit: Boolean) {
        val word = word()
        dragging = false
        selected.clear()
        lastX = Float.NaN
        invalidate()
        if (submit && word.length >= 2) onSubmit?.invoke(word)
    }

    private fun trackTo(x: Float, y: Float) {
        if (lastX.isNaN()) {
            touchLetter(x, y)
        } else {
            val d = hypot(x - lastX, y - lastY)
            val steps = max(1, ceil(d / (letterR * .4f)).toInt())
            for (s in 1..steps) {
                val t = s / steps.toFloat()
                touchLetter(lastX + (x - lastX) * t, lastY + (y - lastY) * t)
            }
        }
        lastX = x; lastY = y
    }

    private fun indexAt(x: Float, y: Float): Int {
        for (i in centers.indices) if (hypot(x - centers[i].x, y - centers[i].y) <= letterR * 1.05f) return i
        return -1
    }

    private fun touchLetter(x: Float, y: Float) {
        val i = indexAt(x, y)
        if (i < 0) return
        when {
            selected.isEmpty() -> select(i)
            i == selected.last() -> Unit
            selected.size >= 2 && i == selected[selected.size - 2] -> { selected.removeAt(selected.lastIndex); SoundManager.play(com.example.wordpuzzle.R.raw.tap, .75f); changed() }
            i !in selected -> select(i)
        }
    }

    private fun select(i: Int) {
        selected += i
        SoundManager.play(com.example.wordpuzzle.R.raw.tap, 1f + .1f * selected.size)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        changed()
    }

    private fun word() = selected.map { letters[it] }.joinToString("")
    private fun changed() { onPreview?.invoke(word()); invalidate() }

    // ---------- drawing ----------
    override fun onDraw(c: Canvas) {
        if (centers.size != letters.size || letters.isEmpty()) return
        c.drawCircle(width / 2f, height / 2f + 4.dp, radius, shadow)
        c.drawCircle(width / 2f, height / 2f, radius, bg)
        c.drawCircle(width / 2f, height / 2f, radius * .86f, ring)
        if (selected.isNotEmpty()) {
            path.reset()
            selected.forEachIndexed { k, i -> if (k == 0) path.moveTo(centers[i].x, centers[i].y) else path.lineTo(centers[i].x, centers[i].y) }
            if (dragging) path.lineTo(fx, fy)
            c.drawPath(path, line)
        }
        var animating = false
        letters.forEachIndexed { i, ch ->
            val sel = i in selected
            val target = if (sel) 1.22f else 1f
            scales[i] += (target - scales[i]) * .3f
            if (abs(target - scales[i]) > .005f) animating = true else scales[i] = target
            val s = scales[i] * appear
            val p = centers[i]
            if (sel) { c.drawCircle(p.x, p.y + 3.dp, letterR * s, shadow); c.drawCircle(p.x, p.y, letterR * s, fill) }
            txt.color = if (sel) Color.WHITE else Palette.LETTER
            txt.textSize = letterR * 1.25f * s
            c.drawText(ch.toString(), p.x, p.y - (txt.ascent() + txt.descent()) / 2, txt)
        }
        if (animating) postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() { appearAnim?.cancel(); super.onDetachedFromWindow() }
}
