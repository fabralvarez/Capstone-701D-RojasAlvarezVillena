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

            normalized.contains("password") -> {
                context.getString(R.string.error_detail_credentials)
            }

            normalized.contains("already") && normalized.contains("email") -> {
                context.getString(R.string.error_detail_email_in_use)
            }

            normalized.contains("email") || normalized.contains("user") -> {
                context.getString(R.string.error_detail_credentials)
            }

            else -> context.getString(R.string.error_detail_generic)
        }
    }
}