package com.example.wordpuzzle.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import com.example.wordpuzzle.R
import com.example.wordpuzzle.databinding.DialogInfoBinding
import kotlin.math.min


object InfoDialog {
    fun show(ctx: Context, title: String, body: CharSequence): Dialog =
        build(ctx, title, tall = true) { b, _ -> b.dlgBody.text = body }

    fun choose(ctx: Context, title: String, items: List<String>, selected: Int, onPick: (Int) -> Unit): Dialog =
        build(ctx, title, tall = false) { b, d ->
            b.dlgBody.visibility = View.GONE
            items.forEachIndexed { i, name ->
                val tv = TextView(ctx).apply {
                    text = if (i == selected) "✓  $name" else name
                    setTextColor(Color.WHITE); textSize = 18f; gravity = Gravity.CENTER
                    setBackgroundResource(if (i == selected) R.drawable.bg_button else R.drawable.bg_button_secondary)
                    setOnClickListener { SoundManager.play(R.raw.click); d.dismiss(); onPick(i) }
                }
                b.dlgContent.addView(tv, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 52.dp.toInt()).apply { topMargin = 10.dp.toInt() })
            }
        }

    private fun build(ctx: Context, title: String, tall: Boolean, fill: (DialogInfoBinding, Dialog) -> Unit): Dialog {
        val b = DialogInfoBinding.inflate(LayoutInflater.from(ctx))
        val d = Dialog(ctx)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(b.root)
        b.dlgTitle.text = title
        b.dlgClose.setOnClickListener { SoundManager.play(R.raw.click); d.dismiss() }
        fill(b, d)
        d.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val dm = ctx.resources.displayMetrics
            setLayout(
                min(dm.widthPixels - 48.dp.toInt(), 420.dp.toInt()),
                if (tall) (dm.heightPixels * .8f).toInt() else ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setDimAmount(.6f)
        }
        b.root.scaleX = .85f; b.root.scaleY = .85f; b.root.alpha = 0f
        b.root.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(260).setInterpolator(OvershootInterpolator()).start()
        d.show()
        return d
    }
}
