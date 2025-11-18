package com.example.vitalarmapp.utils.firebase

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.tasks.await
import com.example.vitalarmapp.models.Medication
import com.example.vitalarmapp.models.Person
import com.example.vitalarmapp.models.User

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
    private const val COLLECTION_PEOPLE = "people"
    private const val COLLECTION_MEDICATIONS = "medications"

    suspend fun registerUser(name: String, email: String, password: String): RegistrationResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser =
                authResult.user ?: return RegistrationResult.UnknownError("Usuario no creado")

            val newUser = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
                createdAt = System.currentTimeMillis()
            )

            db.collection(COLLECTION_USERS)
                .document(firebaseUser.uid)
                .set(newUser)
                .await()

            RegistrationResult.Success(newUser)
        } catch (_: FirebaseAuthUserCollisionException) {
            RegistrationResult.EmailAlreadyInUse
        } catch (_: FirebaseAuthWeakPasswordException) {
            RegistrationResult.WeakPassword
        } catch (e: FirebaseNetworkException) {
            RegistrationResult.ConnectionError(e.localizedMessage)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error registrando usuario: ${e.message}", e)
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
            Log.e("FirebaseManager", "Error en login: ${e.message}", e)
            LoginResult.UnknownError(e.localizedMessage)
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }

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

    suspend fun getPeople(): List<Person> {
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

            val peopleList = result.documents.mapNotNull { document ->
                Log.d("FirebaseDebug", "📄 Procesando documento: ${document.id}")
                val data = document.data ?: return@mapNotNull null

                Person(
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
            // Primero eliminar todos los medicamentos asociados a esta persona
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

            // Luego eliminar la persona
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

    suspend fun getMedicationsForPerson(personId: String): List<Medication> {
        return try {
            Log.d("FirebaseManager", "🔍 Buscando medicamentos para persona: $personId")

            val result = db.collection(COLLECTION_MEDICATIONS)
                .whereEqualTo("personId", personId)
                .get()
                .await()

            Log.d("FirebaseManager", "📄 Documentos encontrados: ${result.documents.size}")

            val medications = result.documents.mapNotNull { document ->
                Log.d("FirebaseManager", "📋 Procesando documento: ${document.id}")
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
                    Log.d("FirebaseManager", "📊 Datos del medicamento: $it")
                }
            }

            Log.d("FirebaseManager", "✅ Medicamentos procesados: ${medications.size}")
            medications

        } catch (e: Exception) {
            Log.e("FirebaseManager", "❌ Error en getMedicationsForPerson: ${e.message}")
            emptyList()
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
                        "FirebaseManager",
                        "⚠️ Documento de usuario no encontrado para ID: $userId"
                    )
                    "Usuario"
                }
            } else {
                Log.w("FirebaseManager", "⚠️ Usuario no autenticado")
                "Usuario"
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "❌ Error obteniendo nombre de usuario: ${e.message}")
            "Usuario"
        }
    }

    private const val COLLECTION_BASE_MEDICATIONS = "base_medications"

    suspend fun addBaseMedication(name: String, description: String? = null): Boolean {
        return try {
            Log.d("FirebaseManager", "🎯 Iniciando addBaseMedication: $name")

            val medicationData = hashMapOf(
                "name" to name,
                "description" to description,
                "createdAt" to System.currentTimeMillis()
            )

            Log.d("FirebaseManager", "📝 Datos del medicamento: $medicationData")

            // Guardar en Firestore
            db.collection(COLLECTION_BASE_MEDICATIONS)
                .add(medicationData)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        "FirebaseManager",
                        "✅ Medicamento base guardado con ID: ${documentReference.id}"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseManager", "❌ Error guardando medicamento base: ${e.message}")
                }
                .await()

            Log.d("FirebaseManager", "🎯 Medicamento base guardado exitosamente")
            true

        } catch (e: Exception) {
            Log.e("FirebaseManager", "❌ Error en addBaseMedication: ${e.message}", e)
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
            Log.e("FirebaseManager", "Error obteniendo medicamentos base: ${e.message}")
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
            Log.e("FirebaseManager", "Error eliminando medicamento base: ${e.message}")
            false
        }
    }

    suspend fun getMedicationUsers(medicationName: String): List<Map<String, Any>> {
        return try {
            Log.d("FirebaseManager", "🔍 Buscando usuarios del medicamento: $medicationName")

            // Buscar en todos los medicamentos de todas las personas
            val result = db.collection(COLLECTION_MEDICATIONS)
                .whereEqualTo("name", medicationName)
                .get()
                .await()

            val usersList = mutableListOf<Map<String, Any>>()

            for (document in result.documents) {
                val medicationData = document.data ?: continue
                val personId = medicationData["personId"] as? String ?: continue

                Log.d("FirebaseManager", "📄 Encontrado para persona: $personId")

                // Obtener información de la persona
                val personDoc = db.collection(COLLECTION_PEOPLE)
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
                    Log.d("FirebaseManager", "✅ Agregado: ${userInfo["personName"]}")
                }
            }

            Log.d("FirebaseManager", "👥 Total de usuarios encontrados: ${usersList.size}")
            usersList

        } catch (e: Exception) {
            Log.e("FirebaseManager", "❌ Error obteniendo usuarios del medicamento: ${e.message}")
            emptyList()
        }
    }
}