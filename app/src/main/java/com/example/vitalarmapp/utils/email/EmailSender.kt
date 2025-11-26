package com.example.vitalarmapp.utils.email

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
        val subject = context.getString(R.string.otp_email_subject)
        val body = context.getString(R.string.otp_email_body, otp)

        val credentialsAvailable = host.isNotBlank() && port.isNotBlank() && username.isNotBlank() && password.isNotBlank()

        if (credentialsAvailable) {
            val smtpResult = withContext(Dispatchers.IO) {
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
                    this.subject = subject
                    setText(body)
                }

                runCatching {
                    Transport.send(message)
                    true
                }.getOrDefault(false)
            }

            if (smtpResult) return true
        }

        return startEmailIntent(context, recipient, subject, body)
    }

    private fun startEmailIntent(context: Context, recipient: String, subject: String, body: String): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        val chooser = Intent.createChooser(intent, context.getString(R.string.otp_email_intent_chooser_title))
        val canHandleIntent = chooser.resolveActivity(context.packageManager) != null
        if (canHandleIntent) {
            context.startActivity(chooser)
        }
        return canHandleIntent
    }
}
