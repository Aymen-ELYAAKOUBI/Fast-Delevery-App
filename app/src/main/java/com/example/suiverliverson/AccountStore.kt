package com.example.suiverliverson

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.security.MessageDigest

enum class AccountRole(val value: String, val label: String) {
    CLIENT("client", "Client"),
    DRIVER("driver", "Livreur"),
    COMPANY("company", "Company");

    companion object {
        fun fromValue(value: String?): AccountRole =
            entries.firstOrNull { it.value == value?.lowercase() } ?: DRIVER
    }
}

enum class AccountApprovalStatus(val value: String) {
    PENDING("pending"),
    APPROVED("approved");

    companion object {
        fun forRole(role: AccountRole): AccountApprovalStatus =
            if (role == AccountRole.CLIENT) APPROVED else PENDING

        fun fromValue(value: String?, role: AccountRole): AccountApprovalStatus =
            entries.firstOrNull { it.value == value?.lowercase() } ?: forRole(role)
    }
}

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
    val location: String = "Casablanca",
    val role: AccountRole = AccountRole.DRIVER,
    val companyName: String = "",
    val approvalStatus: AccountApprovalStatus = AccountApprovalStatus.forRole(role)
)

class AccountStore(context: Context) {
    private val prefs = context.getSharedPreferences("accounts", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth?
        get() = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    private val db: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance(FirebaseFirestoreConfig.DATABASE_ID) }.getOrNull()

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
        put("role", a.role.value)
        put("companyName", a.companyName)
        put("approvalStatus", a.approvalStatus.value)
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
            o.optString("location", "Casablanca"),
            AccountRole.fromValue(o.optString("role", "driver")),
            o.optString("companyName", ""),
            AccountApprovalStatus.fromValue(
                o.optString("approvalStatus").ifEmpty { null },
                AccountRole.fromValue(o.optString("role", "driver"))
            )
        )
    }

    private fun userDocument(account: Account): Map<String, Any> = mapOf(
        "id" to account.id,
        "firstName" to account.firstName,
        "lastName" to account.lastName,
        "age" to account.age,
        "license" to account.license,
        "login" to account.login,
        "imageUri" to (account.imageUri ?: ""),
        "gender" to account.gender,
        "location" to account.location,
        "role" to account.role.value,
        "companyName" to account.companyName,
        "approvalStatus" to account.approvalStatus.value
    )

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
        location: String = "Casablanca",
        role: AccountRole = AccountRole.DRIVER,
        companyName: String = ""
    ): Account {
        val account = Account(
            id = "DRV" + (1000..9999).random(),
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            age = age,
            license = license,
            login = login.trim().lowercase(),
            passwordHash = hash(password),
            imageUri = imageUri,
            gender = gender,
            location = location.trim(),
            role = role,
            companyName = companyName.trim()
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
        role: AccountRole = AccountRole.DRIVER,
        companyName: String = "",
        onResult: (Account?, String?) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            val localAccount = register(
                firstName, lastName, age, license, login, password,
                imageUri, gender, location, role, companyName
            )
            setCurrent(localAccount)
            onResult(localAccount, null)
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(login.trim().lowercase(), password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user == null) {
                    onResult(null, "Firebase did not return the new account")
                    return@addOnSuccessListener
                }

                val account = Account(
                    id = user.uid,
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    age = age,
                    license = license,
                    login = login.trim().lowercase(),
                    passwordHash = hash(password),
                    imageUri = imageUri,
                    gender = gender,
                    location = location.trim(),
                    role = role,
                    companyName = companyName.trim()
                )
                val firestore = db
                if (firestore == null) {
                    user.delete().addOnCompleteListener {
                        logout()
                        onResult(null, "Firebase profile database is unavailable")
                    }
                    return@addOnSuccessListener
                }

                firestore.collection("users").document(user.uid)
                    .set(userDocument(account))
                    .addOnSuccessListener {
                        prefs.edit().putString(key(login), toJson(account)).apply()
                        setCurrent(account)
                        onResult(account, null)
                    }
                    .addOnFailureListener { exception ->
                        user.delete().addOnCompleteListener {
                            logout()
                            onResult(
                                null,
                                exception.localizedMessage ?: "Could not save the account profile"
                            )
                        }
                    }
            }
            .addOnFailureListener { exception ->
                onResult(null, exception.localizedMessage ?: "Failed to create account")
            }
    }

    fun updateAccount(account: Account) {
        prefs.edit().putString(key(account.login), toJson(account)).apply()
        setCurrent(account)

        val uid = auth?.currentUser?.uid ?: account.id
        db?.collection("users")?.document(uid)?.set(userDocument(account))
    }

    fun authenticate(login: String, password: String): Account? {
        val text = prefs.getString(key(login), null) ?: return null
        val account = fromJson(text)
        return if (account.passwordHash == hash(password)) account else null
    }

    fun authenticateWithFirebase(
        login: String,
        password: String,
        expectedRole: AccountRole? = null,
        onResult: (Account?, String?) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            val account = authenticate(login, password)
            if (account == null) {
                onResult(null, "Firebase Authentication is unavailable")
            } else if (expectedRole != null && account.role != expectedRole) {
                onResult(null, "This account is registered as ${account.role.label}")
            } else {
                setCurrent(account)
                onResult(account, null)
            }
            return
        }

        firebaseAuth.signInWithEmailAndPassword(login.trim().lowercase(), password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                val firestore = db
                if (uid == null || firestore == null) {
                    logout()
                    onResult(null, "Could not load the Firebase account profile")
                    return@addOnSuccessListener
                }

                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val account = if (doc.exists()) {
                            Account(
                                id = doc.getString("id") ?: uid,
                                firstName = doc.getString("firstName") ?: "",
                                lastName = doc.getString("lastName") ?: "",
                                age = doc.getLong("age")?.toInt() ?: 0,
                                license = doc.getString("license") ?: "",
                                login = doc.getString("login") ?: login.trim(),
                                passwordHash = hash(password),
                                imageUri = doc.getString("imageUri")?.ifEmpty { null },
                                gender = doc.getString("gender") ?: "man",
                                location = doc.getString("location") ?: "Casablanca",
                                role = AccountRole.fromValue(doc.getString("role")),
                                companyName = doc.getString("companyName") ?: "",
                                approvalStatus = AccountApprovalStatus.fromValue(
                                    doc.getString("approvalStatus"),
                                    AccountRole.fromValue(doc.getString("role"))
                                )
                            )
                        } else {
                            authenticate(login, password)?.copy(
                                id = uid,
                                role = AccountRole.DRIVER,
                                approvalStatus = AccountApprovalStatus.PENDING
                            )
                        }

                        if (account == null) {
                            logout()
                            onResult(null, "This Firebase account has no profile yet")
                        } else if (expectedRole != null && account.role != expectedRole) {
                            logout()
                            onResult(null, "This account is registered as ${account.role.label}")
                        } else {
                            val finishLogin: (Account) -> Unit = { resolvedAccount ->
                                prefs.edit().putString(key(login), toJson(resolvedAccount)).apply()
                                setCurrent(resolvedAccount)
                                onResult(resolvedAccount, null)
                            }
                            if (doc.exists() && doc.contains("approvalStatus")) {
                                finishLogin(account)
                            } else {
                                firestore.collection("users").document(uid)
                                    .set(userDocument(account))
                                    .addOnSuccessListener { finishLogin(account) }
                                    .addOnFailureListener {
                                        logout()
                                        onResult(null, "Could not save the Firebase account profile")
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        logout()
                        onResult(null, exception.localizedMessage ?: "Could not load the account profile")
                    }
            }
            .addOnFailureListener { exception ->
                onResult(null, exception.localizedMessage ?: "Invalid credentials")
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