package com.example.vitalarmapp.utils.local

import android.content.Context
import com.example.vitalarmapp.utils.email.EmailSender
import kotlin.random.Random

object OtpLocalManager {
    private const val OTP_VALIDITY_MILLIS = 5 * 60 * 1000L
    private var currentOtp: String? = null
    private var expirationTime: Long = 0L
    private var storedEmail: String? = null
    private val emailSender = EmailSender()

    suspend fun startSession(context: Context, email: String): Boolean {
        storedEmail = email
        return sendNewOtp(context)
    }

    suspend fun sendNewOtp(context: Context): Boolean {
        val email = storedEmail ?: return false
        val otp = generateOtp()
        currentOtp = otp
        expirationTime = System.currentTimeMillis() + OTP_VALIDITY_MILLIS
        return sendEmail(context, email, otp)
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

    private suspend fun sendEmail(context: Context, email: String, otp: String): Boolean {
        return emailSender.sendOtpEmail(context, email, otp)
    }
}

enum class OtpVerificationResult {
    Success,
    Invalid,
    Expired,
    Missing
}
