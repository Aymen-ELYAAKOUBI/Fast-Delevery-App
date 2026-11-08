package com.example.suiverliverson

import android.content.Context
import org.json.JSONObject
import java.security.MessageDigest

data class Account(
    val id: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val license: String,
    val login: String,
    val passwordHash: String,
    val imageUri: String? = null,
    val gender: String = "man",
    val location: String = "Casablanca"
)

class AccountStore(context: Context) {
    private val prefs = context.getSharedPreferences("accounts", Context.MODE_PRIVATE)

    private fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun key(login: String) = "acc_" + login.trim().lowercase()

    private fun toJson(a: Account): String = JSONObject().apply {
        put("id", a.id)
        put("firstName", a.firstName)
        put("lastName", a.lastName)
        put("age", a.age)
        put("license", a.license)
        put("login", a.login)
        put("passwordHash", a.passwordHash)
        put("imageUri", a.imageUri ?: "")
        put("gender", a.gender)
        put("location", a.location)
    }.toString()

    private fun fromJson(text: String): Account {
        val o = JSONObject(text)
        return Account(
            o.getString("id"),
            o.getString("firstName"),
            o.getString("lastName"),
            o.getInt("age"),
            o.getString("license"),
            o.getString("login"),
            o.getString("passwordHash"),
            if (o.has("imageUri")) o.optString("imageUri").ifEmpty { null } else null,
            o.optString("gender", "man"),
            o.optString("location", "Casablanca")
        )
    }

    fun exists(login: String): Boolean = prefs.contains(key(login))

    fun register(
        firstName: String,
        lastName: String,
        age: Int,
        license: String,
        login: String,
        password: String,
        imageUri: String? = null,
        gender: String = "man",
        location: String = "Casablanca"
    ): Account {
        val account = Account(
            id = "DRV" + (1000..9999).random(),
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            age = age,
            license = license,
            login = login.trim(),
            passwordHash = hash(password),
            imageUri = imageUri,
            gender = gender,
            location = location.trim()
        )
        prefs.edit().putString(key(login), toJson(account)).apply()
        return account
    }

    fun updateAccount(account: Account) {
        prefs.edit().putString(key(account.login), toJson(account)).apply()
        setCurrent(account)
    }

    fun authenticate(login: String, password: String): Account? {
        val text = prefs.getString(key(login), null) ?: return null
        val account = fromJson(text)
        return if (account.passwordHash == hash(password)) account else null
    }

    fun resetPassword(
        login: String,
        lastName: String,
        age: Int,
        newPassword: String
    ): Boolean {
        val text = prefs.getString(key(login), null) ?: return false
        val account = fromJson(text)
        if (!account.lastName.equals(lastName.trim(), ignoreCase = true) ||
            account.age != age
        ) return false
        val updated = account.copy(passwordHash = hash(newPassword))
        prefs.edit().putString(key(login), toJson(updated)).apply()
        return true
    }

    fun setCurrent(account: Account) {
        prefs.edit().putString("current_login", account.login).apply()
    }

    fun current(): Account? {
        val login = prefs.getString("current_login", null) ?: return null
        val text = prefs.getString(key(login), null) ?: return null
        return fromJson(text)
    }

    fun logout() {
        prefs.edit().remove("current_login").apply()
    }
}