package com.example.vitalarmapp.notifications

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangelogRepositoryAdvancedInstrumentedTest {

    private val repository = ChangelogRepository()

    @Test
    fun EmptyChangelogYieldsNoEntries() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val entries = repository.parseEntries(context, "")

        assertTrue(entries.isEmpty())
    }

    @Test
    fun EntriesWithoutHighlightsAreDiscarded() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val changelog = """
            ## [1.2.3] - 2024-05-05
            ### Added
            -
            ### Changed
            -
        """.trimIndent()

        val entries = repository.parseEntries(context, changelog)

        assertTrue(entries.isEmpty())
    }

    @Test
    fun FriendlyMappingsReplaceTechnicalPhrases() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val changelog = """
            ## [2.0.0] - 2024-07-07
            ### Added
            - Mejoras en registro de pacientes y validación de formularios
            ### Changed
            - Estructura del proyecto y compilación del proyecto optimizadas
        """.trimIndent()

        val entries = repository.parseEntries(context, changelog)

        assertEquals(1, entries.size)
        val subhead = entries.first().subhead
        assertTrue(subhead.contains("paciente", ignoreCase = true))
        assertTrue(subhead.contains("formular", ignoreCase = true))
    }

    @Test
    fun LimitsHighlightsToFirstItems() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val changelog = """
            ## [3.1.0] - 2024-08-08
            ### Added
            - Primera mejora
            - Segunda mejora
            - Tercera mejora
            - Cuarta mejora
        """.trimIndent()

        val entries = repository.parseEntries(context, changelog)

        assertEquals(1, entries.size)
        val subhead = entries.first().subhead
        assertTrue(subhead.contains("Primera"))
        assertTrue(subhead.contains("Segunda"))
        assertTrue(subhead.indexOf("Tercera") < 0)
    }
}
