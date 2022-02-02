package cc.memoryhole.scraper.util

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.SocketTimeoutException

// Static client for all WebAPI Instances for better performance (sharing of threadpools / connections)
private val client: OkHttpClient = OkHttpClient()

open class WebAPI {

    /**
     * Calls the getURL function with the provided session cookie
     *
     * @param url URL to Request
     * @return OkHTTP Response
     */
    fun getURL(url: String, cookies: String = ""): Response? {
        val request = Request.Builder()
            .url(url)
            .get()

        if (cookies != "")
            request.addHeader("cookie", cookies)
        try {
            return client.newCall(request.build()).execute()
        } catch (e: SocketTimeoutException) {
            println("!>> ERROR <<!")
            println("")
            println("SocketTimeoutException: ${e.message}".padStart(10, ' '))
            println("")
            println("!>> ERROR <<!")
        }
        return null
    }

    fun getWithSessionCookie(url: String, sessionCookieName: String, sessionID: String): Response? {
        return getURL(url, "${sessionCookieName}=${sessionID}")
    }

    fun getWithSessionCookie(url: String, sessionID: String): Response? {
        return getWithSessionCookie(url, "session_id", sessionID)
    }

    fun downloadToPath(url: String, path: String, filename: String) {
        val response = getURL(url)

        if (response != null && response.isSuccessful) {
            val filePath = File(path)

            if (!filePath.exists())
                filePath.mkdirs()

            val downloadedFile = File(filePath, filename)
            val inputStream = response.body!!.byteStream()

            val input = BufferedInputStream(inputStream)
            val output: OutputStream = FileOutputStream(downloadedFile)

            val data = ByteArray(1024)

            var total: Long = 0

            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
            response.close()
        }
    }
}