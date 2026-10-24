package com.example.shopp.data

import android.content.Context
import android.net.Uri
import com.example.shopp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads product photos to Cloudinary with an unsigned upload preset, so no API secret ships in the app.
 * Cloud name and preset come from `local.properties` (see build.gradle.kts); when they are blank
 * [isConfigured] is false and the UI falls back to pasting image URLs.
 */
@Singleton
class CloudinaryUploader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
    private val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET

    val isConfigured: Boolean get() = cloudName.isNotBlank() && uploadPreset.isNotBlank()

    /** Uploads the image at [uri] and returns its HTTPS URL, resized by Cloudinary for fast loading. */
    suspend fun upload(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            check(isConfigured) { "Image upload is not configured" }
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Could not read the selected image")
            require(bytes.size <= MAX_BYTES) { "Image is larger than 10 MB" }
            Result.success(post(bytes))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun post(bytes: ByteArray): String {
        val boundary = "shopp-" + UUID.randomUUID()
        val connection = URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            connection.outputStream.use { out ->
                fun text(name: String, value: String) {
                    out.write("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n".toByteArray())
                }
                text("upload_preset", uploadPreset)
                out.write(
                    ("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"upload\"\r\n" +
                        "Content-Type: application/octet-stream\r\n\r\n").toByteArray()
                )
                out.write(bytes)
                out.write("\r\n--$boundary--\r\n".toByteArray())
            }

            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(body) }.getOrNull()
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException(json?.optJSONObject("error")?.optString("message") ?: "Upload failed (${connection.responseCode})")
            }
            val url = json?.optString("secure_url").orEmpty()
            check(url.startsWith("https://")) { "Upload returned no image URL" }
            return url
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val MAX_BYTES = 10 * 1024 * 1024
        const val TIMEOUT_MS = 30_000
    }
}
