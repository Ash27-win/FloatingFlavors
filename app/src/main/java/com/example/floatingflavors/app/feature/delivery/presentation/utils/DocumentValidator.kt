package com.example.floatingflavors.app.feature.delivery.presentation.utils

import android.graphics.BitmapFactory
import java.io.File

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

object DocumentValidator {
    private const val MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024L // 5MB
    private const val MAX_PDF_SIZE_BYTES = 10 * 1024 * 1024L // 10MB
    private val ALLOWED_IMAGE_EXTENSIONS = listOf("jpg", "jpeg", "png", "webp")

    fun validateImage(file: File): ValidationResult {
        if (!file.exists()) return ValidationResult.Invalid("File does not exist.")
        if (file.length() == 0L) return ValidationResult.Invalid("File is empty.")
        if (file.length() > MAX_IMAGE_SIZE_BYTES) return ValidationResult.Invalid("Image exceeds 5MB limit.")

        val extension = file.extension.lowercase()
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return ValidationResult.Invalid("Unsupported format. Allowed: JPG, PNG, WEBP.")
        }

        // Deep verify it's an actual decodable image, not just a renamed file
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        if (options.outWidth == -1 || options.outHeight == -1) {
            return ValidationResult.Invalid("Image file is corrupted or invalid.")
        }

        return ValidationResult.Valid
    }

    fun validateDocument(file: File, isPdf: Boolean): ValidationResult {
        if (!file.exists()) return ValidationResult.Invalid("File does not exist.")
        if (file.length() == 0L) return ValidationResult.Invalid("File is empty.")

        if (isPdf) {
            if (file.length() > MAX_PDF_SIZE_BYTES) return ValidationResult.Invalid("PDF exceeds 10MB limit.")
            if (file.extension.lowercase() != "pdf") return ValidationResult.Invalid("File is not a valid PDF.")
            return ValidationResult.Valid
        } else {
            return validateImage(file)
        }
    }
}
