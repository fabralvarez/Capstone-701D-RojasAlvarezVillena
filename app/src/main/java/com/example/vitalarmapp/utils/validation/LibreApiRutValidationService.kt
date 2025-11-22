package com.example.vitalarmapp.utils.validation

import android.util.Log
import com.example.vitalarmapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Valida un RUT chileno contra la API pública de LibreAPI, que mantiene una base de datos
 * actualizada y altamente disponible para verificaciones.
 */
class LibreApiRutValidationService(
    private val client: OkHttpClient = OkHttpClient()
) : RutValidationService {

    override suspend fun isValid(rut: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.libreapi.cl/rut/validate?rut=${rut}")
            .header("Authorization", "Bearer ${BuildConfig.LIBREAPI_TOKEN}")
            .build()

        return@withContext try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(LOG_TAG, "Validación RUT fallida: código ${response.code}")
                    return@use false
                }

                val body = response.body?.string() ?: return@use false
                val json = JSONObject(body)
                json.optBoolean("valid", false)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error verificando RUT: ${e.message}", e)
            false
        }
    }

    companion object {
        private const val LOG_TAG = "RutValidation"
    }
}
