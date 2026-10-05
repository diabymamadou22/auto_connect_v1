package com.example.autoconnect.util

import java.security.MessageDigest

object PasswordHasher {
    fun hash(username: String, password: String): String {
        val input = "${username.lowercase()}:$password:autoconnect_salt"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(username: String, password: String, storedHash: String): Boolean {
        // Support plain text fallback for initial seed
        if (password == storedHash) return true
        val computed = hash(username, password)
        return computed.equals(storedHash, ignoreCase = true)
    }
}
