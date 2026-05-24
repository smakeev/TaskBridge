package com.taskbridge.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Low-level JSON client using Ktor.
 * Performs raw GET requests and deserializes response bodies.
 */
internal class HttpJsonClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Performs a GET request to the specified [url] and deserializes the result as [T].
     */
    suspend inline fun <reified T> getJson(url: String): T {
        return httpClient.get(url).body<T>()
    }
}
