package com.example.vitalarmapp.utils.firebase

import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FirebaseManagerTest {

    @Test
    fun `mapFirestoreException returns permission denied for permission code`() {
        val exception = FirebaseFirestoreException(
            "Missing or insufficient permissions",
            FirebaseFirestoreException.Code.PERMISSION_DENIED
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.PermissionDenied)
        assertEquals("Missing or insufficient permissions", (result as AddPersonResult.PermissionDenied).message)
    }

    @Test
    fun `mapFirestoreException maps unknown errors to UnknownError`() {
        val exception = FirebaseFirestoreException(
            "Unexpected failure",
            FirebaseFirestoreException.Code.DATA_LOSS
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.UnknownError)
        assertEquals("Unexpected failure", (result as AddPersonResult.UnknownError).message)
    }
}
