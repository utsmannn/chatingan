package com.utsman.chatingan.sdk.utils

import java.io.BufferedReader
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object NetworkUtils {
    private fun getRequest(sUrl: String): String? {
        val inputStream: InputStream
        var result: String? = null

        try {
            val url = URL(sUrl)
            val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            conn.connect()
            inputStream = conn.inputStream

            result = inputStream?.bufferedReader()?.use(BufferedReader::readText)
                ?: "error: inputStream is null"
        } catch (err: Error) {
            print("Error when executing get request: " + err.localizedMessage)
        }

        return result
    }



}