package com.example.vitalarmapp.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangelogRepositoryInstrumentedTest {

    @Test
    fun LoadEntriesReturnsFriendlyHighlights() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val repository = ChangelogRepository()

        val entries = repository.loadEntries(context)

        assertFalse(entries.isEmpty())
        assertTrue(entries.all { it.headline.isNotBlank() && it.subhead.isNotBlank() })
    }

    @Test
    fun ParseEntriesFiltersAndSummarizesSections() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val repository = ChangelogRepository()
        val changelogText = """
            ## [0.9.99] - 2024-12-01
            ### Added
            - Registro de pacientes con validación estricta
            - Listados y navegación más ágiles
            ### Changed
            - Interfaz de usuario con temas Material Design 3
            - Librerias y dependencias actualizadas
        """.trimIndent()

        val entries = repository.parseEntries(context, changelogText)

        assertEquals(1, entries.size)
        val entry = entries.first()
        assertTrue(entry.headline.contains("0.9.99"))
        assertTrue(entry.subhead.contains("paciente", ignoreCase = true) || entry.subhead.contains("interfaz", ignoreCase = true))
    }
}
