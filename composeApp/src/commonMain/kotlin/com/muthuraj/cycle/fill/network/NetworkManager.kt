/* $Id$ */
package com.muthuraj.cycle.fill.network

import androidx.compose.runtime.mutableStateOf
import com.muthuraj.cycle.fill.provideHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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

    suspend fun getCategories(): Response<CategoryResponse> {
        return httpClient.get(BASE_URL) {
            parameter("endpoint", "categories")
        }.body<Response<CategoryResponse>>()
            .let { response ->
                response.copy(data = response.data?.map {
                    it.copy(icon = "$BASE_SERVER_URL/${it.icon}")
                })
            }
    }

    suspend fun getSubCategories(categoryId: Int): Response<SubCategoryResponse> {
        return httpClient.get(BASE_URL) {
            parameter("endpoint", "subcategories")
            parameter("categoryId", categoryId)
        }.body<Response<SubCategoryResponse>>()
            .let { response ->
                response.copy(data = response.data?.map {
                    it.copy(icon = "$BASE_SERVER_URL/${it.icon}")
                })
            }
    }

    suspend fun getCollections(subCategoryId: Int): Response<CollectionResponse> {
        return httpClient.get(BASE_URL) {
            parameter("endpoint", "collections")
            parameter("subCategoryId", subCategoryId)
        }.body()
    }

    suspend fun getItems(collectionId: Int): Response<ItemResponse> {
        return httpClient.get(BASE_URL) {
            parameter("endpoint", "items")
            parameter("collectionId", collectionId)
        }.body()
    }

    suspend fun getAllItems(): Response<ItemDetailedResponse> {
        return httpClient.get(BASE_URL) {
            parameter("endpoint", "items-detailed")
        }.body()
    }

    suspend fun addCategory(name: String, icon: String): PostResponse {
        return httpClient.post(BASE_URL) {
            parameter("endpoint", "categories")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("name", name)
                put("icon", icon)
            })
        }.body()
    }

    suspend fun adSubCategory(name: String, icon: String, categoryId: Int): PostResponse {
        return httpClient.post(BASE_URL) {
            parameter("endpoint", "subcategories")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("name", name)
                put("icon", icon)
                put("categoryId", categoryId)
            })
        }.body()
    }

    suspend fun addCollection(name: String, subCategoryId: Int): PostResponse {
        return httpClient.post(BASE_URL) {
            parameter("endpoint", "collections")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("name", name)
                put("subCategoryId", subCategoryId)
            })
        }.body()
    }

    suspend fun addItem(date: String, description: String, collectionId: Int): PostResponse {
        return httpClient.post(BASE_URL) {
            parameter("endpoint", "items")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("date", date)
                put("description", description)
                put("collectionId", collectionId)
            })
        }.body()
    }

    suspend fun updateItemDescription(itemId: Int, description: String): PostResponse {
        return httpClient.put(BASE_URL) {
            parameter("endpoint", "items")
            parameter("id", itemId)
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("description", description)
            })
        }.body()
    }

    suspend fun deleteItem(itemId: Int): PostResponse {
        return httpClient.delete(BASE_URL) {
            parameter("endpoint", "items")
            parameter("id", itemId)
        }.body()
    }

    suspend fun deleteCollection(collectionId: Int): PostResponse {
        return httpClient.delete(BASE_URL) {
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