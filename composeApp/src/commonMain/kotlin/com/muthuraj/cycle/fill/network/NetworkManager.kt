/* $Id$ */
package com.muthuraj.cycle.fill.network

import androidx.compose.runtime.mutableStateOf
import com.muthuraj.cycle.fill.provideHttpClient
import com.muthuraj.cycle.fill.util.log
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.tatarka.inject.annotations.Inject

/**
 * Created by Muthuraj on 14/12/24.
 */
@Inject
class NetworkManager {
    private val httpClient by lazy {
        provideHttpClient()
    }

    /**
     * Runs [request] against the currently selected server URL. If it fails because the
     * host could not be reached — a connect timeout or an unresolvable address, e.g. the
     * local IP has no route because we're away from home — flips [useTailScaleUrl] and
     * retries once against the other URL.
     *
     * Only connection-establishment failures are retried. Since the connection was never
     * opened, the server never received the request, so retrying a write (POST/PUT/DELETE)
     * cannot double-apply it. Every other failure (HTTP 4xx/5xx, read/request timeouts
     * where the server *did* receive the request) propagates untouched.
     *
     * The flag is sticky for the session, so once the working URL is found subsequent
     * requests go straight to it without paying the connect-timeout penalty again.
     */
    private suspend fun <T> withUrlFallback(request: suspend () -> T): T {
        return try {
            request()
        } catch (e: Exception) {
            if (e !is ConnectTimeoutException && e !is UnresolvedAddressException) {
                throw e
            }
            val from = if (useTailScaleUrl) "tailscale" else "local"
            val to = if (useTailScaleUrl) "local" else "tailscale"
            log { "$from URL unreachable (${e.message}); retrying on $to URL" }
            useTailScaleUrl = !useTailScaleUrl
            request()
        }
    }

    suspend fun getCategories(): Response<CategoryResponse> = withUrlFallback {
        httpClient.get(BASE_URL) {
            parameter("endpoint", "categories")
        }.body<Response<CategoryResponse>>()
            .let { response ->
                response.copy(data = response.data?.map {
                    it.copy(icon = "$BASE_SERVER_URL/${it.icon}")
                })
            }
    }

    suspend fun getSubCategories(categoryId: Int): Response<SubCategoryResponse> = withUrlFallback {
        httpClient.get(BASE_URL) {
            parameter("endpoint", "subcategories")
            parameter("categoryId", categoryId)
        }.body<Response<SubCategoryResponse>>()
            .let { response ->
                response.copy(data = response.data?.map {
                    it.copy(icon = "$BASE_SERVER_URL/${it.icon}")
                })
            }
    }

    suspend fun getCollections(subCategoryId: Int): Response<CollectionResponse> = withUrlFallback {
        httpClient.get(BASE_URL) {
            parameter("endpoint", "collections")
            parameter("subCategoryId", subCategoryId)
        }.body()
    }

    suspend fun getItems(collectionId: Int): Response<ItemResponse> = withUrlFallback {
        httpClient.get(BASE_URL) {
            parameter("endpoint", "items")
            parameter("collectionId", collectionId)
        }.body()
    }

    suspend fun getAllItems(): Response<ItemDetailedResponse> = withUrlFallback {
        httpClient.get(BASE_URL) {
            parameter("endpoint", "items-detailed")
        }.body()
    }

    suspend fun addCategory(name: String, icon: String): PostResponse = withUrlFallback {
        httpClient.post(BASE_URL) {
            parameter("endpoint", "categories")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("name", name)
                put("icon", icon)
            })
        }.body()
    }

    suspend fun adSubCategory(name: String, icon: String, categoryId: Int): PostResponse = withUrlFallback {
        httpClient.post(BASE_URL) {
            parameter("endpoint", "subcategories")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("name", name)
                put("icon", icon)
                put("categoryId", categoryId)
            })
        }.body()
    }

    suspend fun addCollection(name: String, subCategoryId: Int): PostResponse = withUrlFallback {
        httpClient.post(BASE_URL) {
            parameter("endpoint", "collections")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("name", name)
                put("subCategoryId", subCategoryId)
            })
        }.body()
    }

    suspend fun addItem(date: String, description: String, collectionId: Int): PostResponse = withUrlFallback {
        httpClient.post(BASE_URL) {
            parameter("endpoint", "items")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("date", date)
                put("description", description)
                put("collectionId", collectionId)
            })
        }.body()
    }

    suspend fun updateItemDescription(itemId: Int, description: String): PostResponse = withUrlFallback {
        httpClient.put(BASE_URL) {
            parameter("endpoint", "items")
            parameter("id", itemId)
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("description", description)
            })
        }.body()
    }

    suspend fun deleteItem(itemId: Int): PostResponse = withUrlFallback {
        httpClient.delete(BASE_URL) {
            parameter("endpoint", "items")
            parameter("id", itemId)
        }.body()
    }

    suspend fun deleteCollection(collectionId: Int): PostResponse = withUrlFallback {
        httpClient.delete(BASE_URL) {
            parameter("endpoint", "collections")
            parameter("id", collectionId)
        }.body()
    }

    companion object {
        private const val TAIL_SCALE_URL = ""
        private const val LOCAL_URL =
            ""
        var useTailScaleUrl = false

        private val BASE_SERVER_URL
            get() = if (useTailScaleUrl) TAIL_SCALE_URL else LOCAL_URL
        private val BASE_URL
            get() = "$BASE_SERVER_URL/index.php"
    }
}