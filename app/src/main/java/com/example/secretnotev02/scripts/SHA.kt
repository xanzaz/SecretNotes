package com.example.secretnotes.scripts

import java.security.MessageDigest

fun sha256(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
    return hashBytes.joinToString("") { "%02x".format(it) } // Преобразование в hex-строку
}

fun sha512(input: String): String {
    val digest = MessageDigest.getInstance("SHA-512")
    val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
    return hashBytes.joinToString("") { "%02x".format(it) } // Преобразование в hex-строку
}