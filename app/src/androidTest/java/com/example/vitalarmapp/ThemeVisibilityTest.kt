@file:Suppress("SameParameterValue")

package com.example.vitalarmapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.ColorUtils
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.MaterialShapeDrawable
import kotlin.math.max
import kotlin.math.min
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeVisibilityTest {

    @Test
    fun importantElementsMaintainContrastInLightAndDarkMode() {
        verifyContrastForMode(AppCompatDelegate.MODE_NIGHT_NO, "modo claro")
        verifyContrastForMode(AppCompatDelegate.MODE_NIGHT_YES, "modo oscuro")
    }

    private fun verifyContrastForMode(@AppCompatDelegate.NightMode mode: Int, modeLabel: String) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val loginButton = activity.findViewById<MaterialButton>(R.id.main_login_btn)
            val signUpButton = activity.findViewById<MaterialButton>(R.id.main_signup_btn)
            val toolbar = activity.findViewById<MaterialToolbar>(R.id.main_toolbar)

            assertViewHasContrast(loginButton, "Botón de inicio de sesión en $modeLabel")
            assertViewHasContrast(signUpButton, "Botón de registro en $modeLabel")
            assertTextContrast(
                toolbar.resolveColor(android.R.attr.textColorPrimary),
                resolveBackgroundColor(toolbar) ?: resolveWindowBackground(activity.findViewById(android.R.id.content)),
                "Título del toolbar en $modeLabel"
            )
        }
        scenario.close()
    }

    private fun assertViewHasContrast(textView: TextView, description: String) {
        val backgroundColor = resolveBackgroundColor(textView) ?: resolveWindowBackground(textView)
        assertTextContrast(textView.currentTextColor, backgroundColor, description)
    }

    private fun assertTextContrast(@ColorInt foreground: Int, @ColorInt background: Int, description: String) {
        val contrast = calculateContrastRatio(foreground, background)
        require(contrast >= MIN_CONTRAST_RATIO) {
            "Él $description no tiene suficiente contraste. Ratio actual: $contrast"
        }
    }

    private fun calculateContrastRatio(@ColorInt foreground: Int, @ColorInt background: Int): Double {
        val foregroundLuminance = ColorUtils.calculateLuminance(foreground) + 0.05
        val backgroundLuminance = ColorUtils.calculateLuminance(background) + 0.05
        return max(foregroundLuminance, backgroundLuminance) / min(foregroundLuminance, backgroundLuminance)
    }

    private fun resolveBackgroundColor(view: View): Int? {
        view.backgroundTintList?.defaultColor?.let { return it }
        return when (val background = view.background) {
            is ColorDrawable -> background.color
            is MaterialShapeDrawable -> background.fillColor?.defaultColor
            else -> null
        }
    }

    @ColorInt
    private fun View.resolveColor(@AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun resolveWindowBackground(view: View): Int {
        val typedValue = TypedValue()
        val context = view.context
        val resolved = if (context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)) {
            typedValue.data
        } else {
            Color.BLACK
        }
        return resolved
    }

    private companion object {
        private const val MIN_CONTRAST_RATIO = 1.0
    }
}