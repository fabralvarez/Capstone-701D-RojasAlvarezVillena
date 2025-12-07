package com.example.vitalarmapp.adapters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationFormTest {

    @Test
    fun TabletSupportsSolidWeightUnits() {
        assertEquals(listOf("mg", "mcg", "g"), MedicationForm.TABLET.allowedUnits)
        assertTrue(MedicationForm.TABLET.allowedUnits.none { it.contains("ml") })
    }

    @Test
    fun SyrupSupportsLiquidUnits() {
        assertTrue(MedicationForm.SYRUP.allowedUnits.contains("ml"))
        assertTrue(MedicationForm.SYRUP.allowedUnits.contains("mg/ml"))
    }

    @Test
    fun OtherIncludesAllDefaultUnits() {
        val expected = listOf("mg", "mcg", "g", "ml")
        assertEquals(expected, MedicationForm.OTHER.allowedUnits)
    }
}
