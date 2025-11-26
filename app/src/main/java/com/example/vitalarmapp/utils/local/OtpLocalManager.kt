package com.example.vitalarmapp.utils.local

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.vitalarmapp.R
import kotlin.random.Random

object OtpLocalManager {
    private const val OTP_VALIDITY_MILLIS = 5 * 60 * 1000L
    private var currentOtp: String? = null
    private var expirationTime: Long = 0L
    private var storedEmail: String? = null

    fun startSession(context: Context, email: String): Boolean {
        storedEmail = email
        return sendNewOtp(context)
    }

    fun sendNewOtp(context: Context): Boolean {
        val email = storedEmail ?: return false
        val otp = generateOtp()
        currentOtp = otp
        expirationTime = System.currentTimeMillis() + OTP_VALIDITY_MILLIS
        return launchEmailIntent(context, email, otp)
    }

    fun verifyOtp(input: String): OtpVerificationResult {
        val savedOtp = currentOtp ?: return OtpVerificationResult.Missing
        if (System.currentTimeMillis() > expirationTime) {
            return OtpVerificationResult.Expired
        }
        return if (input == savedOtp) OtpVerificationResult.Success else OtpVerificationResult.Invalid
    }

    fun clearSession() {
        currentOtp = null
        storedEmail = null
        expirationTime = 0L
    }

    private fun generateOtp(): String = Random.nextInt(100_000, 1_000_000).toString()

    private fun launchEmailIntent(context: Context, email: String, otp: String): Boolean {
        val mailUri = "mailto:$email".toUri()
        val mailIntent = Intent(Intent.ACTION_SENDTO, mailUri).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.otp_email_subject))
            putExtra(
                Intent.EXTRA_TEXT,
                context.getString(R.string.otp_email_body, otp)
            )
        }

        if (mailIntent.resolveActivity(context.packageManager) == null) return false

        val chooserIntent = Intent.createChooser(
            mailIntent,
            context.getString(R.string.otp_email_intent_chooser_title)
        )

        return runCatching {
            context.startActivity(chooserIntent)
            true
        }.getOrDefault(false)
    }
}

enum class OtpVerificationResult {
    Success,
    Invalid,
    Expired,
    Missing
}
