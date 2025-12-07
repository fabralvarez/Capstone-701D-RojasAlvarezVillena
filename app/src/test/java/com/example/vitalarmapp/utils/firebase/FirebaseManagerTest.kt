package com.example.vitalarmapp.utils.firebase

import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseManagerTest {

    @Test
    fun PermissionDeniedMapsToPermissionResult() {
        val exception = FirebaseFirestoreException(
            "Forbidden",
            FirebaseFirestoreException.Code.PERMISSION_DENIED
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.PermissionDenied)
    }

    @Test
    fun UnauthenticatedMapsToAuthError() {
        val exception = FirebaseFirestoreException(
            "Unauthenticated",
            FirebaseFirestoreException.Code.UNAUTHENTICATED
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.AuthError)
    }

    @Test
    fun UnavailableMapsToServiceUnavailable() {
        val exception = FirebaseFirestoreException(
            "Service unavailable",
            FirebaseFirestoreException.Code.UNAVAILABLE
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.ServiceUnavailable)
    }

    @Test
    fun DeadlineExceededMapsToTimeout() {
        val exception = FirebaseFirestoreException(
            "deadline",
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.Timeout)
    }

    @Test
    fun ResourceExhaustedMapsToQuotaExceeded() {
        val exception = FirebaseFirestoreException(
            "quota",
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.QuotaExceeded)
    }

    @Test
    fun InvalidArgumentMapsToInvalidData() {
        val exception = FirebaseFirestoreException(
            "invalid",
            FirebaseFirestoreException.Code.INVALID_ARGUMENT
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.InvalidData)
    }

    @Test
    fun FailedPreconditionMapsToInvalidData() {
        val exception = FirebaseFirestoreException(
            "precondition",
            FirebaseFirestoreException.Code.FAILED_PRECONDITION
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.InvalidData)
    }

    @Test
    fun CancelledMapsToOperationCancelled() {
        val exception = FirebaseFirestoreException(
            "cancelled",
            FirebaseFirestoreException.Code.CANCELLED
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.OperationCancelled)
    }

    @Test
    fun OtherCodesMapToUnknownError() {
        val exception = FirebaseFirestoreException(
            "other",
            FirebaseFirestoreException.Code.INTERNAL
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertEquals(AddPersonResult.UnknownError("other"), result)
    }

    @Test
    fun AbortedMapsToServiceUnavailable() {
        val exception = FirebaseFirestoreException(
            "aborted",
            FirebaseFirestoreException.Code.ABORTED
        )

        val result = FirebaseManager.mapFirestoreException(exception)

        assertTrue(result is AddPersonResult.ServiceUnavailable)
    }
}
