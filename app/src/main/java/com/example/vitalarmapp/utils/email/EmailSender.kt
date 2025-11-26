package com.example.vitalarmapp.utils.email

import android.content.Context
import com.example.vitalarmapp.BuildConfig
import com.example.vitalarmapp.R
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailSender {
    suspend fun sendOtpEmail(context: Context, recipient: String, otp: String): Boolean {
        val host = BuildConfig.SMTP_HOST
        val port = BuildConfig.SMTP_PORT
        val username = BuildConfig.SMTP_USERNAME
        val password = BuildConfig.SMTP_PASSWORD

        if (host.isBlank() || port.isBlank() || username.isBlank() || password.isBlank()) {
            return false
        }

        return withContext(Dispatchers.IO) {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", host)
                put("mail.smtp.port", port)
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username, context.getString(R.string.app_name)))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
                subject = context.getString(R.string.otp_email_subject)
                setText(context.getString(R.string.otp_email_body, otp))
            }

            runCatching {
                Transport.send(message)
                true
            }.getOrDefault(false)
        }
    }
}
