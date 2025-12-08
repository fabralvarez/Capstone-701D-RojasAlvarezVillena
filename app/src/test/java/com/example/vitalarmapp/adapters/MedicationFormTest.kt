package com.example.vitalarmapp.adapters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationFormTest {

    @Test
    fun tabletSupportsSolidWeightUnits() {
        assertEquals(listOf("mg", "mcg", "g"), MedicationForm.TABLET.allowedUnits)
        assertTrue(MedicationForm.TABLET.allowedUnits.none { it.contains("ml") })
    }

    @Test
    fun syrupSupportsLiquidUnits() {
        assertTrue(MedicationForm.SYRUP.allowedUnits.contains("ml"))
        assertTrue(MedicationForm.SYRUP.allowedUnits.contains("mg/ml"))
    }

    @Test
    fun otherIncludesAllDefaultUnits() {
        val expected = listOf("mg", "mcg", "g", "ml")
        assertEquals(expected, MedicationForm.OTHER.allowedUnits)
    }
}
