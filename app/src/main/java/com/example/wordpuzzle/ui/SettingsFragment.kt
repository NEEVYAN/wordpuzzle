package com.example.wordpuzzle.ui

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.wordpuzzle.R
import com.example.wordpuzzle.databinding.FragmentSettingsBinding
import java.util.Locale

object Languages {
    val all = listOf("en" to "English", "hi" to "हिन्दी", "es" to "Español")

    fun current(): String {
        val l = AppCompatDelegate.getApplicationLocales()
        val lang = if (!l.isEmpty) l[0]?.language else Locale.getDefault().language
        return if (all.any { it.first == lang }) lang!! else "en"
    }

    fun name(tag: String) = all.first { it.first == tag }.second

    fun set(tag: String) {
        if (tag != current()) AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}

class SettingsFragment : Fragment() {
    companion object { const val TAG = "settings" }

    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!
    private var dialog: Dialog? = null
    private var leaving = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSettingsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        b.btnBack.setOnClickListener {
            SoundManager.play(R.raw.click)
            if (!leaving && !parentFragmentManager.isStateSaved) { leaving = true; parentFragmentManager.popBackStack() }
        }
        b.swMusic.isChecked = MusicManager.enabled
        b.swMusic.setOnCheckedChangeListener { _, on -> MusicManager.setEnabled(on) }
        b.rowMusic.setOnClickListener { b.swMusic.toggle() }

        b.swSound.isChecked = SoundManager.enabled
        b.swSound.setOnCheckedChangeListener { _, on -> SoundManager.setEnabled(on); if (on) SoundManager.play(R.raw.click) }
        b.rowSound.setOnClickListener { b.swSound.toggle() }

        b.tvLanguage.text = Languages.name(Languages.current())
        b.rowLanguage.setOnClickListener { click(); chooseLanguage() }
        b.rowFaq.setOnClickListener { click(); dialog = InfoDialog.show(requireContext(), getString(R.string.faq), faqText()) }
        b.rowPrivacy.setOnClickListener { click(); dialog = InfoDialog.show(requireContext(), getString(R.string.privacy_policy), getString(R.string.privacy_text)) }
        b.rowTerms.setOnClickListener { click(); dialog = InfoDialog.show(requireContext(), getString(R.string.terms), getString(R.string.terms_text)) }

        val ver = runCatching { requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName }.getOrNull() ?: "1.0"
        b.tvVersion.text = getString(R.string.version, ver)
    }

    private fun click() = SoundManager.play(R.raw.click)

    private fun chooseLanguage() {
        val tags = Languages.all.map { it.first }
        dialog = InfoDialog.choose(requireContext(), getString(R.string.choose_language), Languages.all.map { it.second }, tags.indexOf(Languages.current())) {
            Languages.set(tags[it])
        }
    }

    private fun faqText(): CharSequence {
        val qs = resources.getStringArray(R.array.faq_questions)
        val ans = resources.getStringArray(R.array.faq_answers)
        val sb = SpannableStringBuilder()
        qs.indices.forEach { i ->
            val st = sb.length
            sb.append(qs[i])
            sb.setSpan(StyleSpan(Typeface.BOLD), st, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(ForegroundColorSpan(Palette.BONUS), st, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.append("\n").append(ans[i]).append("\n\n")
        }
        return sb
    }

    override fun onDestroyView() {
        dialog?.dismiss(); dialog = null
        _b = null
        super.onDestroyView()
    }
}
