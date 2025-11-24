package com.example.vitalarmapp.utils.local

import android.content.Context
import com.example.vitalarmapp.R

object ErrorMessageTranslator {
    fun toSpanish(context: Context, rawMessage: String?): String? {
        if (rawMessage.isNullOrBlank()) return null
        val normalized = rawMessage.lowercase()
        return when {
            normalized.contains("network") ||
                    normalized.contains("timeout") ||
                    normalized.contains("connection") -> {
                context.getString(R.string.error_detail_network)
            }

            normalized.contains("weak") && normalized.contains("password") -> {
                context.getString(R.string.error_detail_weak_password)
            }

            normalized.contains("badly formatted") ||
                    normalized.contains("invalid email") ||
                    normalized.contains("email address is badly formatted") -> {
                context.getString(R.string.error_detail_invalid_email)
            }

            normalized.contains("missing password") ||
                    normalized.contains("empty password") -> {
                context.getString(R.string.error_detail_missing_password)
            }

            normalized.contains("password") -> {
                context.getString(R.string.error_detail_credentials)
            }

            normalized.contains("already") && normalized.contains("email") -> {
                context.getString(R.string.error_detail_email_in_use)
            }

            normalized.contains("email") || normalized.contains("user") -> {
                context.getString(R.string.error_detail_credentials)
            }

            normalized.contains("too many") ||
                    normalized.contains("attempts") ||
                    normalized.contains("too-many-requests") -> {
                context.getString(R.string.error_detail_too_many_requests)
            }

            normalized.contains("operation not allowed") -> {
                context.getString(R.string.error_detail_operation_not_allowed)
            }

            normalized.contains("disabled") && normalized.contains("user") -> {
                context.getString(R.string.error_detail_user_disabled)
            }

            normalized.contains("permission") && normalized.contains("denied") -> {
                context.getString(R.string.error_detail_permission_denied)
            }

            else -> context.getString(R.string.error_detail_generic)
        }
    }
}
