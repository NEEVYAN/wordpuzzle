package com.example.wordpuzzle.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random


class BubblesView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) : View(ctx, attrs) {
    private class B(val x: Float, var y: Float, val r: Float, val speed: Float, val phase: Float)
    private val bubbles = ArrayList<B>()
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x14FFFFFF }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1.5f.dp; color = 0x33FFFFFF }
    private var last = 0L

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        bubbles.clear()
        repeat(16) {
            bubbles += B(Random.nextFloat(), Random.nextFloat(), (10 + Random.nextInt(40)).dp,
                .015f + Random.nextFloat() * .035f, Random.nextFloat() * 6f)
        }
    }

    override fun onDraw(c: Canvas) {
        val now = SystemClock.uptimeMillis()
        val dt = if (last == 0L) 0f else ((now - last) / 1000f).coerceAtMost(.05f)
        last = now
        val t = now / 1000f
        for (b in bubbles) {
            b.y -= b.speed * dt
            if (b.y * height + b.r < 0) b.y = 1f + b.r / height
            val x = b.x * width + sin(t * .8f + b.phase) * 14.dp
            val y = b.y * height
            c.drawCircle(x, y, b.r, fill)
            c.drawCircle(x, y, b.r, ring)
        }
        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() { last = 0L; super.onDetachedFromWindow() }
}
