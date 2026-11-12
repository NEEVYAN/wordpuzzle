package com.example.wordpuzzle.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random


class ConfettiView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) : View(ctx, attrs) {
    private class P(var x: Float, var y: Float, var vx: Float, var vy: Float, val size: Float, val color: Int, var rot: Float, val vr: Float)
    private val ps = ArrayList<P>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colors = intArrayOf(Palette.ACCENT, Palette.OK, Palette.BONUS, Palette.PREVIEW, 0xFFFF5C93.toInt(), 0xFF36D1DC.toInt())
    private var last = 0L

    fun burst() {
        repeat(90) {
            ps += P(width / 2f, height * .4f, (Random.nextFloat() * 2 - 1) * 280.dp, -(240 + Random.nextFloat() * 420).dp,
                (5 + Random.nextInt(6)).dp, colors.random(), Random.nextFloat() * 360, (Random.nextFloat() * 2 - 1) * 540)
        }
        last = SystemClock.uptimeMillis()
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        if (ps.isEmpty()) return
        val now = SystemClock.uptimeMillis()
        val dt = ((now - last) / 1000f).coerceAtMost(.05f)
        last = now
        val it = ps.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.vy += 900.dp * dt
            p.x += p.vx * dt; p.y += p.vy * dt; p.rot += p.vr * dt
            if (p.y > height + 40) { it.remove(); continue }
            paint.color = p.color
            c.save(); c.rotate(p.rot, p.x, p.y)
            c.drawRect(p.x - p.size, p.y - p.size / 2, p.x + p.size, p.y + p.size / 2, paint)
            c.restore()
        }
        if (ps.isNotEmpty()) postInvalidateOnAnimation()
    }
}
