package com.taskbridge.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Low-level JSON client using Ktor.
 * Performs raw GET requests and deserializes response bodies.
 */
internal class HttpJsonClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            val jsonConfig = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            json(jsonConfig)
            // GitHub Raw serves .json files as text/plain; register JSON for that too.
            json(jsonConfig, ContentType.Text.Plain)
        }
    }

    /**
     * Performs a GET request to the specified [url] and deserializes the result as [T].
     */
    suspend inline fun <reified T> getJson(url: String): T {
        return httpClient.get(url).body<T>()
    }
}
