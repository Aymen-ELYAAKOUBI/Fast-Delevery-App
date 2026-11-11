package com.example.suiverliverson

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private val auth: FirebaseAuth?
        get() = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    private val db: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

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

    fun registerWithFirebase(
        firstName: String,
        lastName: String,
        age: Int,
        license: String,
        login: String,
        password: String,
        imageUri: String? = null,
        gender: String = "man",
        location: String = "Casablanca",
        onResult: (Account?, String?) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            val localAccount = register(firstName, lastName, age, license, login, password, imageUri, gender, location)
            setCurrent(localAccount)
            onResult(localAccount, null)
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(login.trim(), password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ("DRV" + (1000..9999).random())
                val account = Account(
                    id = uid,
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

                val userMap = mapOf(
                    "id" to account.id,
                    "firstName" to account.firstName,
                    "lastName" to account.lastName,
                    "age" to account.age,
                    "license" to account.license,
                    "login" to account.login,
                    "imageUri" to (account.imageUri ?: ""),
                    "gender" to account.gender,
                    "location" to account.location
                )
                db?.collection("users")?.document(uid)?.set(userMap)

                prefs.edit().putString(key(login), toJson(account)).apply()
                setCurrent(account)
                onResult(account, null)
            }
            .addOnFailureListener { exception ->
                onResult(null, exception.localizedMessage ?: "Failed to create account")
            }
    }

    fun updateAccount(account: Account) {
        prefs.edit().putString(key(account.login), toJson(account)).apply()
        setCurrent(account)

        val uid = auth?.currentUser?.uid ?: account.id
        val userMap = mapOf(
            "id" to account.id,
            "firstName" to account.firstName,
            "lastName" to account.lastName,
            "age" to account.age,
            "license" to account.license,
            "login" to account.login,
            "imageUri" to (account.imageUri ?: ""),
            "gender" to account.gender,
            "location" to account.location
        )
        db?.collection("users")?.document(uid)?.set(userMap)
    }

    fun authenticate(login: String, password: String): Account? {
        val text = prefs.getString(key(login), null) ?: return null
        val account = fromJson(text)
        return if (account.passwordHash == hash(password)) account else null
    }

    fun authenticateWithFirebase(
        login: String,
        password: String,
        onResult: (Account?, String?) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            val account = authenticate(login, password)
            if (account != null) {
                setCurrent(account)
                onResult(account, null)
            } else {
                onResult(null, "Account not found")
            }
            return
        }

        firebaseAuth.signInWithEmailAndPassword(login.trim(), password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                db?.collection("users")?.document(uid)?.get()
                    ?.addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val account = Account(
                                id = doc.getString("id") ?: uid,
                                firstName = doc.getString("firstName") ?: "",
                                lastName = doc.getString("lastName") ?: "",
                                age = doc.getLong("age")?.toInt() ?: 25,
                                license = doc.getString("license") ?: "B",
                                login = doc.getString("login") ?: login.trim(),
                                passwordHash = hash(password),
                                imageUri = doc.getString("imageUri")?.ifEmpty { null },
                                gender = doc.getString("gender") ?: "man",
                                location = doc.getString("location") ?: "Casablanca"
                            )
                            prefs.edit().putString(key(login), toJson(account)).apply()
                            setCurrent(account)
                            onResult(account, null)
                        } else {
                            val local = authenticate(login, password)
                            if (local != null) setCurrent(local)
                            onResult(local, null)
                        }
                    }
                    ?.addOnFailureListener {
                        val local = authenticate(login, password)
                        if (local != null) setCurrent(local)
                        onResult(local, null)
                    }
            }
            .addOnFailureListener { exception ->
                val local = authenticate(login, password)
                if (local != null) {
                    setCurrent(local)
                    onResult(local, null)
                } else {
                    onResult(null, exception.localizedMessage ?: "Invalid credentials")
                }
            }
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

    fun sendPasswordResetEmail(login: String, onResult: (Boolean, String?) -> Unit) {
        val firebaseAuth = auth
        if (firebaseAuth != null && login.isNotBlank()) {
            firebaseAuth.sendPasswordResetEmail(login.trim())
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { onResult(false, it.localizedMessage) }
        } else {
            onResult(false, "Firebase Auth not available")
        }
    }

    fun setCurrent(account: Account) {
        prefs.edit().putString("current_login", account.login).apply()
    }

    fun current(): Account? {
        val firebaseAuth = auth
        if (firebaseAuth?.currentUser != null) {
            val login = firebaseAuth.currentUser?.email ?: prefs.getString("current_login", null)
            if (login != null) {
                val text = prefs.getString(key(login), null)
                if (text != null) return fromJson(text)
            }
        }
        val login = prefs.getString("current_login", null) ?: return null
        val text = prefs.getString(key(login), null) ?: return null
        return fromJson(text)
    }

    fun logout() {
        runCatching { auth?.signOut() }
        prefs.edit().remove("current_login").apply()
    }
}