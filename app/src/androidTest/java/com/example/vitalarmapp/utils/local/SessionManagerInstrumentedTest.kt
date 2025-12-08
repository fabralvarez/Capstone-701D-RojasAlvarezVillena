package com.example.vitalarmapp.utils.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionManagerInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("vitalarm_preferences", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test
    fun shouldToggleSessionPreference() {
        assertFalse(SessionManager.shouldKeepSession(context))

        SessionManager.setKeepSession(context, true)

        assertTrue(SessionManager.shouldKeepSession(context))
    }

    @Test
    fun shouldToggleMaterialYouPreference() {
        SessionManager.setMaterialYouEnabled(context, true)
        assertTrue(SessionManager.isMaterialYouEnabled(context))

        SessionManager.setMaterialYouEnabled(context, false)
        assertFalse(SessionManager.isMaterialYouEnabled(context))
    }

    @Test
    fun shouldToggleAutoClearCachePreference() {
        SessionManager.setAutoClearCacheEnabled(context, true)
        assertTrue(SessionManager.isAutoClearCacheEnabled(context))

        SessionManager.setAutoClearCacheEnabled(context, false)
        assertFalse(SessionManager.isAutoClearCacheEnabled(context))
    }

    @Test
    fun clearAppCacheRemovesTemporaryFiles() {
        val cacheDir = context.cacheDir
        val nestedFile = cacheDir.resolve("tempDir/test.txt")
        nestedFile.parentFile?.mkdirs()
        nestedFile.writeText("temp data")

        val cleared = SessionManager.clearAppCache(context)

        assertTrue(cleared)
        assertFalse(nestedFile.exists())
    }
}
