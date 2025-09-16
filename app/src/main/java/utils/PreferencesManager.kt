package com.example.vitalarmapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.vitalarmapp.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("VitalarmPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString("current_user", userJson).apply()
    }

    fun getCurrentUser(): User? {
        val userJson = sharedPreferences.getString("current_user", null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun saveRegisteredUsers(users: List<User>) {
        val usersJson = gson.toJson(users)
        sharedPreferences.edit().putString("registered_users", usersJson).apply()
    }

    fun getRegisteredUsers(): List<User> {
        val usersJson = sharedPreferences.getString("registered_users", null)
        return if (usersJson != null) {
            val type: Type = object : TypeToken<List<User>>() {}.type
            gson.fromJson(usersJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun userExists(email: String): Boolean {
        val users = getRegisteredUsers()
        return users.any { it.correo == email }
    }

    fun logout() {
        sharedPreferences.edit().remove("current_user").apply()
    }

    fun saveSessionToken(token: String) {
        sharedPreferences.edit().putString("session_token", token).apply()
    }

    fun getSessionToken(): String? {
        return sharedPreferences.getString("session_token", null)
    }

    fun isLoggedIn(): Boolean {
        return getCurrentUser() != null && getSessionToken() != null
    }
}