package com.example.vitalarmapp.utils.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.vitalarmapp.R
import kotlin.random.Random

object OtpLocalManager {
    private const val OtpValidityMillis = 5 * 60 * 1000L
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
        expirationTime = System.currentTimeMillis() + OtpValidityMillis
        return launchEmailIntent(context, email, otp)
    }

    fun verifyOtp(input: String): OtpVerificationResult {
        val savedOtp = currentOtp ?: return OtpVerificationResult.Missing
        if (System.currentTimeMillis() > expirationTime) {
            return OtpVerificationResult.Expired
        }
        return if (input == savedOtp) OtpVerificationResult.Success else OtpVerificationResult.Invalid
    }

    fun getEmail(): String? = storedEmail

    fun clearSession() {
        currentOtp = null
        storedEmail = null
        expirationTime = 0L
    }

    private fun generateOtp(): String = Random.nextInt(100_000, 1_000_000).toString()

    private fun launchEmailIntent(context: Context, email: String, otp: String): Boolean {
        val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.otp_email_subject))
            putExtra(
                Intent.EXTRA_TEXT,
                context.getString(R.string.otp_email_body, otp)
            )
        }
        return mailIntent.resolveActivity(context.packageManager) != null &&
            runCatching { context.startActivity(mailIntent) }.isSuccess
    }
}

enum class OtpVerificationResult {
    Success,
    Invalid,
    Expired,
    Missing
}
