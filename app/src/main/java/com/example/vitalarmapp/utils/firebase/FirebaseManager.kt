@file:Suppress("unused", "RedundantSuppression")

package com.example.vitalarmapp.utils.firebase

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.tasks.await
import com.example.vitalarmapp.models.AlarmRecord
import com.example.vitalarmapp.models.Medication
import com.example.vitalarmapp.models.Patient
import com.example.vitalarmapp.models.User
import com.example.vitalarmapp.adapters.MedicationForm
import com.example.vitalarmapp.adapters.MedicationSearchItem
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

sealed class AddPersonResult {
    data object Success : AddPersonResult()
    data class AuthError(val message: String? = null) : AddPersonResult()
    data class ConnectionError(val message: String? = null) : AddPersonResult()
    data class PermissionDenied(val message: String? = null) : AddPersonResult()
    data class ServiceUnavailable(val message: String? = null) : AddPersonResult()
    data class Timeout(val message: String? = null) : AddPersonResult()
    data class QuotaExceeded(val message: String? = null) : AddPersonResult()
    data class InvalidData(val message: String? = null) : AddPersonResult()
    data class OperationCancelled(val message: String? = null) : AddPersonResult()
    data class UnknownError(val message: String? = null) : AddPersonResult()
}

object FirebaseManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    private val db: FirebaseFirestore = Firebase.firestore
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_PATIENTS = "patients"
    private const val COLLECTION_MEDICATIONS = "medications"
    private const val COLLECTION_REGISTERED_MEDICATIONS = "registered_medications"
    private const val COLLECTION_ALARMS = "alarms"
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
        birthDate: String,
        gender: String,
        notes: String,
    ): AddPersonResult {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e(LOG_TAG, "Error añadiendo persona: usuario no autenticado")
            return AddPersonResult.AuthError()
        }

        return try {
            val userName = auth.currentUser?.displayName ?: getCurrentUserName()

            val personData = mutableMapOf<String, Any>(
                "name" to name,
                "birthDate" to birthDate,
                "gender" to gender,
                "notes" to notes,
                "userId" to userId,
                "userName" to userName,
                "createdAt" to System.currentTimeMillis()
            )

            patientsCollection(userId)
                .add(personData)
                .await()
            AddPersonResult.Success
        } catch (e: FirebaseNetworkException) {
            Log.e(LOG_TAG, "Error de red añadiendo persona: ${e.message}", e)
            AddPersonResult.ConnectionError(e.localizedMessage)
        } catch (e: FirebaseFirestoreException) {
            Log.e(LOG_TAG, "Error de Firestore añadiendo persona: ${e.message}", e)
            mapFirestoreException(e)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error añadiendo persona: ${e.message}", e)
            AddPersonResult.UnknownError(e.localizedMessage)
        }
    }

    @VisibleForTesting
    internal fun mapFirestoreException(e: FirebaseFirestoreException): AddPersonResult {
        return when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                AddPersonResult.PermissionDenied(e.localizedMessage)

            FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                AddPersonResult.AuthError(e.localizedMessage)

            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.ABORTED ->
                AddPersonResult.ServiceUnavailable(e.localizedMessage)

            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                AddPersonResult.Timeout(e.localizedMessage)

            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                AddPersonResult.QuotaExceeded(e.localizedMessage)

            FirebaseFirestoreException.Code.INVALID_ARGUMENT,
            FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                AddPersonResult.InvalidData(e.localizedMessage)

            FirebaseFirestoreException.Code.CANCELLED ->
                AddPersonResult.OperationCancelled(e.localizedMessage)

            else -> AddPersonResult.UnknownError(e.localizedMessage)
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

            val result = patientsCollection(userId)
                .get()
                .await()

            Log.d("FirebaseDebug", "✅ Consulta completada. Documentos: ${result.documents.size}")

            val peopleList = result.documents.mapNotNull { document ->
                Log.d("FirebaseDebug", "📄 Procesando documento: ${document.id}")
                val data = document.data ?: return@mapNotNull null

                Patient(
                    id = document.id,
                    name = data["name"] as? String ?: "",
                    birthDate = data["birthDate"] as? String ?: "",
                    gender = data["gender"] as? String ?: "",
                    notes = data["notes"] as? String ?: "",
                    userId = data["userId"] as? String ?: "",
                    userName = data["userName"] as? String ?: "",
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
        val userId = getCurrentUserId() ?: return false

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
            patientsCollection(userId)
                .document(personId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error eliminando persona: ${e.message}")
            false
        }
    }

    private fun patientsCollection(userId: String) =
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_PATIENTS)

    private fun registeredMedicationsCollection(userId: String) =
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_REGISTERED_MEDICATIONS)

    suspend fun saveRegisteredMedication(medication: MedicationSearchItem): Boolean {
        val userId = getCurrentUserId() ?: return false

        return try {
            val medicationData = mapOf(
                "name" to medication.name,
                "indication" to medication.indication,
                "pharmacology" to medication.pharmacology,
                "route" to medication.route,
                "composition" to medication.composition,
                "dosageValue" to medication.dosageValue,
                "dosageUnit" to medication.dosageUnit,
                "form" to medication.form?.name,
                "createdAt" to System.currentTimeMillis(),
            )

            registeredMedicationsCollection(userId)
                .add(medicationData)
                .await()
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error guardando medicamento: ${e.message}", e)
            false
        }
    }

    suspend fun getRegisteredMedications(): List<RegisteredMedication> {
        val userId = getCurrentUserId() ?: return emptyList()

        return try {
            val result = registeredMedicationsCollection(userId)
                .orderBy("createdAt")
                .get()
                .await()

            result.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null

                val formName = data["form"] as? String
                val medication = MedicationSearchItem(
                    name = data["name"] as? String ?: "",
                    indication = data["indication"] as? String,
                    pharmacology = data["pharmacology"] as? String,
                    route = data["route"] as? String,
                    composition = data["composition"] as? String,
                    dosageValue = data["dosageValue"] as? String,
                    dosageUnit = data["dosageUnit"] as? String,
                    form = formName?.let { MedicationForm.valueOf(it) },
                )

                RegisteredMedication(
                    id = document.id,
                    medication = medication,
                )
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error obteniendo medicamentos registrados: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun deleteRegisteredMedications(ids: List<String>): Boolean {
        val userId = getCurrentUserId() ?: return false
        if (ids.isEmpty()) return true

        return try {
            db.runBatch { batch ->
                ids.forEach { id ->
                    val docRef = registeredMedicationsCollection(userId).document(id)
                    batch.delete(docRef)
                }
            }.await()
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error eliminando medicamentos: ${e.message}", e)
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

    suspend fun addAlarm(record: AlarmRecord): Boolean {
        val userId = getCurrentUserId() ?: return false
        return try {
            alarmsCollection(userId)
                .document(record.id)
                .set(record)
                .await()
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error guardando alarma: ${e.message}", e)
            false
        }
    }

    suspend fun getAlarms(): List<AlarmRecord> {
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val result = alarmsCollection(userId)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                doc.toObject(AlarmRecord::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error obteniendo alarmas: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAlarmById(id: String): AlarmRecord? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val snapshot = alarmsCollection(userId)
                .document(id)
                .get()
                .await()
            snapshot.toObject(AlarmRecord::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error obteniendo alarma: ${e.message}", e)
            null
        }
    }

    suspend fun markAlarmTriggered(id: String, timestamp: Long = System.currentTimeMillis()) {
        val userId = getCurrentUserId() ?: return
        try {
            alarmsCollection(userId)
                .document(id)
                .update("triggeredAt", timestamp)
                .await()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error marcando alarma como disparada: ${e.message}", e)
        }
    }

    suspend fun markAlarmVerified(id: String, photoPath: String) {
        val userId = getCurrentUserId() ?: return
        try {
            val updates = mapOf(
                "photoPath" to photoPath,
                "triggeredAt" to System.currentTimeMillis(),
            )
            alarmsCollection(userId)
                .document(id)
                .update(updates)
                .await()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "❌ Error marcando alarma verificada: ${e.message}", e)
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

    private fun alarmsCollection(userId: String) = db.collection(COLLECTION_USERS)
        .document(userId)
        .collection(COLLECTION_ALARMS)
}

internal interface PatientRegistrar {
    fun getCurrentUserId(): String?
    suspend fun addPerson(
        name: String,
        birthDate: String,
        gender: String,
        notes: String,
    ): AddPersonResult
}

internal object FirebasePatientRegistrar : PatientRegistrar {
    override fun getCurrentUserId(): String? = FirebaseManager.getCurrentUserId()

    override suspend fun addPerson(
        name: String,
        birthDate: String,
        gender: String,
        notes: String,
    ): AddPersonResult = FirebaseManager.addPerson(name, birthDate, gender, notes)
}

data class RegisteredMedication(
    val id: String,
    val medication: MedicationSearchItem,
)
