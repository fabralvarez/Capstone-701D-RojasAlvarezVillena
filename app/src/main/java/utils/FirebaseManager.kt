package com.example.vitalarmapp.utils

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    // Instancias de Firebase
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    @SuppressLint("StaticFieldLeak")
    private val db: FirebaseFirestore = Firebase.firestore

    // Colecciones de Firestore
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_PEOPLE = "people"
    private const val COLLECTION_MEDICATIONS = "medications"

    // ==================== AUTHENTICATION ====================

    suspend fun registerUser(email: String, password: String, name: String): Boolean {
        return try {
            Log.d("FirebaseDebug", "🎯 Registrando: $email")

            // Solo crear usuario en Authentication
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            if (result.user != null) {
                Log.d("FirebaseDebug", "✅ Usuario AUTENTICADO: ${result.user!!.uid}")

                // Intentar guardar en Firestore, pero si falla no es crítico
                try {
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection(COLLECTION_USERS).document(result.user!!.uid)
                        .set(userData)
                        .await()

                    Log.d("FirebaseDebug", "✅ Datos guardados en Firestore")
                } catch (firestoreError: Exception) {
                    Log.w(
                        "FirebaseDebug",
                        "⚠️  No se pudo guardar en Firestore, pero el usuario está creado: ${firestoreError.message}"
                    )
                }

                true // SIEMPRE retorna true si el usuario se creó en Auth
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "❌ Error crítico en registro: ${e.message}")
            false
        }
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user != null
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error en login: ${e.message}")
            false
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }

    // ==================== PERSONAS A CUIDADO ====================

    suspend fun addPerson(name: String, birthDate: String? = null): Boolean {
        val userId = getCurrentUserId() ?: return false

        return try {
            val personData = hashMapOf(
                "name" to name,
                "birthDate" to birthDate,
                "userId" to userId,
                "createdAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_PEOPLE)
                .add(personData)
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error añadiendo persona: ${e.message}")
            false
        }
    }

    suspend fun getPeople(): List<Map<String, Any>> {
        val userId = getCurrentUserId()

        Log.d("FirebaseDebug", "🔍 Buscando personas para userId: $userId")

        if (userId == null) {
            Log.e("FirebaseDebug", "❌ userId es null - usuario no autenticado")
            return emptyList()
        }

        return try {
            Log.d("FirebaseDebug", "🎯 Consultando Firestore...")

            val result = db.collection(COLLECTION_PEOPLE)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            Log.d("FirebaseDebug", "✅ Consulta completada. Documentos: ${result.documents.size}")

            val peopleList = result.documents.map { document ->
                Log.d("FirebaseDebug", "📄 Procesando documento: ${document.id}")
                val data = document.data ?: emptyMap()
                mutableMapOf<String, Any>(
                    "id" to document.id
                ).apply {
                    putAll(data)
                }
            }

            Log.d("FirebaseDebug", "👥 Personas procesadas: ${peopleList.size}")
            peopleList

        } catch (e: Exception) {
            Log.e("FirebaseDebug", "❌ Error en getPeople: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deletePerson(personId: String): Boolean {
        return try {
            db.collection(COLLECTION_PEOPLE)
                .document(personId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error eliminando persona: ${e.message}")
            false
        }
    }

    // ==================== MEDICAMENTOS ====================

    suspend fun addMedication(
        personId: String,
        name: String,
        dosage: String,
        frequency: String,
        alarmTimes: List<String>
    ): Boolean {
        return try {
            Log.d("FirebaseManager", "💊 Guardando medicamento: $name para persona: $personId")

            val medicationData = hashMapOf(
                "personId" to personId,
                "name" to name,
                "dosage" to dosage,
                "frequency" to frequency,
                "alarmTimes" to alarmTimes,
                "createdAt" to System.currentTimeMillis()
            )

            Log.d("FirebaseManager", "📝 Datos del medicamento: $medicationData")

            // Guardar en Firestore
            db.collection(COLLECTION_MEDICATIONS)
                .add(medicationData)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        "FirebaseManager",
                        "✅ Medicamento guardado con ID: ${documentReference.id}"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseManager", "❌ Error guardando medicamento: ${e.message}")
                }
                .await()

            Log.d("FirebaseManager", "🎯 Medicamento guardado exitosamente")
            true

        } catch (e: Exception) {
            Log.e("FirebaseManager", "❌ Error en addMedication: ${e.message}", e)
            false
        }
    }

    suspend fun getMedicationsForPerson(personId: String): List<Map<String, Any>> {
        return try {
            Log.d("FirebaseManager", "🔍 Buscando medicamentos para persona: $personId")

            val result = db.collection(COLLECTION_MEDICATIONS)
                .whereEqualTo("personId", personId)
                .get()
                .await()

            Log.d("FirebaseManager", "📄 Documentos encontrados: ${result.documents.size}")

            val medications = result.documents.map { document ->
                Log.d("FirebaseManager", "📋 Procesando documento: ${document.id}")
                val data = document.data ?: emptyMap()
                mutableMapOf<String, Any>(
                    "id" to document.id
                ).apply {
                    putAll(data)
                    Log.d("FirebaseManager", "📊 Datos del medicamento: $this")
                }
            }

            Log.d("FirebaseManager", "✅ Medicamentos procesados: ${medications.size}")
            medications

        } catch (e: Exception) {
            Log.e("FirebaseManager", "❌ Error en getMedicationsForPerson: ${e.message}")
            emptyList()
        }
    }
}