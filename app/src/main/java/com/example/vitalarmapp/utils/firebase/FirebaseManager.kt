package com.example.vitalarmapp.utils.firebase

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.tasks.await
import com.example.vitalarmapp.models.Medication
import com.example.vitalarmapp.models.Patient
import com.example.vitalarmapp.models.User
import com.google.firebase.Firebase

sealed class LoginResult {
    data object Success : LoginResult()
    data object UserNotFound : LoginResult()
    data class ConnectionError(val message: String? = null) : LoginResult()
    data class UnknownError(val message: String? = null) : LoginResult()
}

sealed class RegistrationResult {
    data class Success(val user: User) : RegistrationResult()
    data object EmailAlreadyInUse : RegistrationResult()
    data object WeakPassword : RegistrationResult()
    data class ConnectionError(val message: String? = null) : RegistrationResult()
    data class UnknownError(val message: String? = null) : RegistrationResult()
}

object FirebaseManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    private val db: FirebaseFirestore = Firebase.firestore
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_PATIENTS = "patients"
    private const val COLLECTION_MEDICATIONS = "medications"
    private const val LOG_TAG = "FirebaseManager"

    suspend fun registerUser(
        name: String,
        rut: String,
        email: String,
        password: String
    ): RegistrationResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser =
                authResult.user ?: return RegistrationResult.UnknownError("Usuario no creado")

            val creationTimestamp = System.currentTimeMillis()
            val newUser = User(
                id = firebaseUser.uid,
                name = name,
                rut = rut,
                email = email,
                createdAt = creationTimestamp
            )

            val profileData = hashMapOf(
                "id" to firebaseUser.uid,
                "name" to name,
                "rut" to rut,
                "email" to email,
                "createdAt" to creationTimestamp
            )

            db.collection(COLLECTION_USERS)
                .document(firebaseUser.uid)
                .set(profileData)
                .await()

            RegistrationResult.Success(newUser)
        } catch (_: FirebaseAuthUserCollisionException) {
            RegistrationResult.EmailAlreadyInUse
        } catch (_: FirebaseAuthWeakPasswordException) {
            RegistrationResult.WeakPassword
        } catch (e: FirebaseNetworkException) {
            RegistrationResult.ConnectionError(e.localizedMessage)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error registrando usuario: ${e.message}", e)
            auth.currentUser?.delete()
            RegistrationResult.UnknownError(e.localizedMessage)
        }
    }

    suspend fun loginAndVerifyUser(email: String, password: String): LoginResult {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return LoginResult.UnknownError("Usuario autenticado inválido")

            val userDocument = db.collection(COLLECTION_USERS)
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!userDocument.exists()) {
                auth.signOut()
                LoginResult.UserNotFound
            } else {
                LoginResult.Success
            }
        } catch (e: FirebaseNetworkException) {
            LoginResult.ConnectionError(e.localizedMessage)
        } catch (_: FirebaseAuthInvalidUserException) {
            LoginResult.UserNotFound
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error en login: ${e.message}", e)
            LoginResult.UnknownError(e.localizedMessage)
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun addPerson(
        name: String,
        birthDate: String? = null,
        gender: String? = null,
        notes: String? = null
    ): Boolean {
        val userId = getCurrentUserId() ?: return false

        return try {
            val personData = hashMapOf(
                "name" to name,
                "birthDate" to birthDate,
                "gender" to gender,
                "notes" to notes,
                "userId" to userId,
                "createdAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_PATIENTS)
                .add(personData)
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error añadiendo persona: ${e.message}")
            false
        }
    }

    suspend fun getPeople(): List<Patient> {
        val userId = getCurrentUserId()

        Log.d("FirebaseDebug", "🔍 Buscando personas para userId: $userId")

        if (userId == null) {
            Log.e("FirebaseDebug", "❌ userId es null - usuario no autenticado")
            return emptyList()
        }

        return try {
            Log.d("FirebaseDebug", "🎯 Consultando Firestore...")

            val result = db.collection(COLLECTION_PATIENTS)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            Log.d("FirebaseDebug", "✅ Consulta completada. Documentos: ${result.documents.size}")

            val peopleList = result.documents.mapNotNull { document ->
                Log.d("FirebaseDebug", "📄 Procesando documento: ${document.id}")
                val data = document.data ?: return@mapNotNull null

                Patient(
                    id = document.id,
                    name = data["name"] as? String ?: "",
                    birthDate = data["birthDate"] as? String,
                    userId = data["userId"] as? String ?: "",
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
                )
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
            val medications = getMedicationsForPerson(personId)
            medications.forEach { medication ->
                val medId = medication.id
                if (medId.isNotEmpty()) {
                    db.collection(COLLECTION_MEDICATIONS)
                        .document(medId)
                        .delete()
                        .await()
                }
            }
            db.collection(COLLECTION_PATIENTS)
                .document(personId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error eliminando persona: ${e.message}")
            false
        }
    }

    suspend fun addMedication(
        personId: String,
        name: String,
        dosage: String,
        frequency: String,
        alarmTimes: List<String>
    ): Boolean {
        return try {
            Log.d(LOG_TAG, "💊 Guardando medicamento: $name para persona: $personId")

            val medicationData = hashMapOf(
                "personId" to personId,
                "name" to name,
                "dosage" to dosage,
                "frequency" to frequency,
                "alarmTimes" to alarmTimes,
                "createdAt" to System.currentTimeMillis()
            )

            Log.d(LOG_TAG, "📝 Datos del medicamento: $medicationData")
            val documentReference = db.collection(COLLECTION_MEDICATIONS)
                .add(medicationData)
                .await()

            Log.d(LOG_TAG, "🎯 Medicamento guardado exitosamente con ID: ${documentReference.id}")
            true

        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error en addMedication: ${e.message}", e)
            false
        }
    }

    suspend fun getMedicationsForPerson(personId: String): List<Medication> {
        return try {
            Log.d(LOG_TAG, "🔍 Buscando medicamentos para persona: $personId")

            val result = db.collection(COLLECTION_MEDICATIONS)
                .whereEqualTo("personId", personId)
                .get()
                .await()

            Log.d(LOG_TAG, "📄 Documentos encontrados: ${result.documents.size}")

            val medications = result.documents.mapNotNull { document ->
                Log.d(LOG_TAG, "📋 Procesando documento: ${document.id}")
                val data = document.data ?: return@mapNotNull null
                Medication(
                    id = document.id,
                    personId = data["personId"] as? String ?: "",
                    name = data["name"] as? String ?: "",
                    dosage = data["dosage"] as? String ?: "",
                    frequency = data["frequency"] as? String ?: "",
                    alarmTimes = (data["alarmTimes"] as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.sorted()
                        ?: emptyList(),
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
                ).also {
                    Log.d(LOG_TAG, "📊 Datos del medicamento: $it")
                }
            }

            Log.d(LOG_TAG, "✅ Medicamentos procesados: ${medications.size}")
            medications

        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error en getMedicationsForPerson: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateMedicationAlarmTimes(
        medicationId: String,
        alarmTimes: List<String>,
    ): Boolean {
        return try {
            db.collection(COLLECTION_MEDICATIONS)
                .document(medicationId)
                .update("alarmTimes", alarmTimes.sorted())
                .await()
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error actualizando alarmas del medicamento: ${e.message}", e)
            false
        }
    }

    suspend fun getCurrentUserName(): String {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val document = db.collection(COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .await()

                if (document.exists()) {
                    val name = document.getString("name")
                    name ?: "Usuario"
                } else {
                    Log.w(
                        LOG_TAG,
                        "⚠️ Documento de usuario no encontrado para ID: $userId"
                    )
                    "Usuario"
                }
            } else {
                Log.w(LOG_TAG, "⚠️ Usuario no autenticado")
                "Usuario"
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error obteniendo nombre de usuario: ${e.message}")
            "Usuario"
        }
    }

    suspend fun getCurrentUserProfile(): User? {
        return try {
            val userId = getCurrentUserId() ?: return null
            val document = db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                User(
                    id = document.getString("id") ?: userId,
                    name = document.getString("name") ?: "",
                    rut = document.getString("rut") ?: "",
                    email = document.getString("email") ?: "",
                    createdAt = (document.getLong("createdAt") ?: 0L)
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error obteniendo perfil: ${e.message}")
            null
        }
    }

    suspend fun updateCurrentUserName(newName: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            db.collection(COLLECTION_USERS)
                .document(userId)
                .update("name", newName)
                .await()
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error actualizando nombre de usuario: ${e.message}", e)
            false
        }
    }

    private const val COLLECTION_BASE_MEDICATIONS = "base_medications"

    suspend fun addBaseMedication(name: String, description: String? = null): Boolean {
        return try {
            Log.d(LOG_TAG, "🎯 Iniciando addBaseMedication: $name")

            val medicationData = hashMapOf(
                "name" to name,
                "description" to description,
                "createdAt" to System.currentTimeMillis()
            )

            Log.d(LOG_TAG, "📝 Datos del medicamento: $medicationData")

            val documentReference = db.collection(COLLECTION_BASE_MEDICATIONS)
                .add(medicationData)
                .await()

            Log.d(LOG_TAG, "🎯 Medicamento base guardado exitosamente con ID: ${documentReference.id}")
            true

        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error en addBaseMedication: ${e.message}", e)
            false
        }
    }

    suspend fun getBaseMedications(): List<Map<String, Any>> {
        return try {
            val result = db.collection(COLLECTION_BASE_MEDICATIONS)
                .orderBy("name")
                .get()
                .await()

            result.documents.map { document ->
                mutableMapOf<String, Any>(
                    "id" to document.id
                ).apply {
                    putAll(document.data ?: emptyMap())
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error obteniendo medicamentos base: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteBaseMedication(medicationId: String): Boolean {
        return try {
            db.collection(COLLECTION_BASE_MEDICATIONS)
                .document(medicationId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error eliminando medicamento base: ${e.message}")
            false
        }
    }

    suspend fun getMedicationUsers(medicationName: String): List<Map<String, Any>> {
        return try {
            Log.d(LOG_TAG, "🔍 Buscando usuarios del medicamento: $medicationName")
            val result = db.collection(COLLECTION_MEDICATIONS)
                .whereEqualTo("name", medicationName)
                .get()
                .await()

            val usersList = mutableListOf<Map<String, Any>>()

            for (document in result.documents) {
                val medicationData = document.data ?: continue
                val personId = medicationData["personId"] as? String ?: continue

                Log.d(LOG_TAG, "📄 Encontrado para persona: $personId")
                val personDoc = db.collection(COLLECTION_PATIENTS)
                    .document(personId)
                    .get()
                    .await()

                if (personDoc.exists()) {
                    val personData = personDoc.data ?: emptyMap()
                    val userInfo = mutableMapOf<String, Any>().apply {
                        put("personName", personData["name"] as? String ?: "Sin nombre")
                        put("dosage", medicationData["dosage"] as? String ?: "Sin dosis")
                        put("frequency", medicationData["frequency"] as? String ?: "Sin frecuencia")
                        put(
                            "alarmTimes",
                            medicationData["alarmTimes"] as? List<*> ?: emptyList<String>()
                        )
                    }
                    usersList.add(userInfo)
                    Log.d(LOG_TAG, "✅ Agregado: ${userInfo["personName"]}")
                }
            }

            Log.d(LOG_TAG, "👥 Total de usuarios encontrados: ${usersList.size}")
            usersList

        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error obteniendo usuarios del medicamento: ${e.message}")
            emptyList()
        }
    }

}